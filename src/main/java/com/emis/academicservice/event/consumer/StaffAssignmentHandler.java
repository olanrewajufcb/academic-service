package com.emis.academicservice.event.consumer;

import com.emis.academicservice.event.ConsumedEventRepository;
import com.emis.academicservice.event.domain.ConsumedEvent;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.event.domain.StaffAssignedEvent;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Slf4j
public class StaffAssignmentHandler {

    private final ClassSectionRepository classSectionRepository;
    private final ConsumedEventRepository consumedEventRepository;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;

    public Mono<Void> handle(DomainEvent<JsonNode> event) {
        log.info("Inside handle method :::::: {}", event);

        return consumedEventRepository.existsById(event.getEventId())
            .doOnSubscribe(s -> log.info("Subscribed to handle Mono for eventId: {}", event.getEventId()))
            .doOnNext(alreadyProcessed -> log.info("EventId: {}, alreadyProcessed: {}", event.getEventId(), alreadyProcessed))
            .flatMap(alreadyProcessed -> {
                if (Boolean.TRUE.equals(alreadyProcessed)) {
                    log.info("Event {} already processed, skipping", event.getEventId());
                    return Mono.empty();
                }
                log.info("Processing event now ::::::::: {}", event.getEventId());
                return applyAssignment(event)
                    .then(markEventConsumed(event))
                    .as(transactionalOperator::transactional)
                    .doOnSuccess(v -> log.info("Successfully processed event {}", event.getEventId()))
                    .doOnError(e -> log.error("Error processing event {}", event.getEventId(), e));
            })
            .doOnTerminate(() -> log.info("Terminated handle Mono for eventId: {}", event.getEventId()));
    }

    private Mono<Void> applyAssignment(DomainEvent<JsonNode> event) {

        StaffAssignedEvent payload =
                objectMapper.convertValue(
                        event.getData(),
                        StaffAssignedEvent.class
                );
        log.info("Logging the staff assignment event ::::::: {}", event.getData());

        return classSectionRepository
            .findById(payload.getSectionId())
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Class section not found with the given id: " + payload.getSectionId())))
            .flatMap(section -> {

                // Idempotent guard
                Long existingTeacher = section.getTeacherId();
                Long incomingTeacher = payload.getStaffId();
                log.info("Existing teacher: {}, Incoming teacher: {}", existingTeacher, incomingTeacher);

                if (existingTeacher != null && existingTeacher.equals(incomingTeacher)) {
                    return Mono.empty();
                }


                section.setTeacherId(payload.getStaffId());
                section.setStaffCode(payload.getStaffCode());
                section.setTeacherName(payload.getStaffName());

                return classSectionRepository.save(section)
                        .doOnSuccess(saved ->
                                log.info("Assigned teacher {} to section {}",
                                        payload.getStaffId(),
                                        section.getSectionId()))
                        .then();
            });
    }


    private Mono<Void> markEventConsumed(DomainEvent<JsonNode> event) {

        return consumedEventRepository.save(
             (ConsumedEvent.builder()
                     .eventId(event.getEventId())
                     .eventType(event.getEventType())
                     .consumedAt(java.time.Instant.now())
                     .build())
        ).then();
    }
}