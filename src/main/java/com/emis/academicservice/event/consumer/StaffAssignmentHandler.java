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

        return consumedEventRepository.existsById(event.getEventId())
            .flatMap(alreadyProcessed -> {
                if (Boolean.TRUE.equals(alreadyProcessed)) {
                    log.info("Event {} already processed, skipping", event.getEventId());
                    return Mono.empty();
                }

                return applyAssignment(event)
                    .then(markEventConsumed(event))
                    .as(transactionalOperator::transactional);
            });
    }

    private Mono<Void> applyAssignment(DomainEvent<JsonNode> event) {

        StaffAssignedEvent payload =
                objectMapper.convertValue(
                        event.getData(),
                        StaffAssignedEvent.class
                );

        return classSectionRepository
            .findById(payload.getSectionId())
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Class section not found")))
            .flatMap(section -> {

                // Idempotent guard
                if (payload.getStaffId().equals(section.getTeacherId())) {
                    return Mono.empty();
                }

                section.setTeacherId(payload.getStaffId());
                section.setTeacherCode(payload.getStaffCode());
                section.setTeacherName(payload.getStaffName());

                return classSectionRepository.save(section).then();
            });
    }


    private Mono<Void> markEventConsumed(DomainEvent<JsonNode> event) {

        return consumedEventRepository.save(
             (ConsumedEvent.builder()
                     .eventId(event.getEventId())
                     .eventType(event.getEventType())
                     .build())
        ).then();
    }
}