package com.emis.academicservice.event.consumer;

import com.emis.academicservice.event.ConsumedEventRepository;
import com.emis.academicservice.event.domain.ConsumedEvent;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.event.domain.StaffTransferredEvent;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class StaffTransferEventHandler {

    private final ClassSectionRepository classSectionRepository;
    private final ConsumedEventRepository consumedEventRepository;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;


    public Mono<Void> handle(DomainEvent<JsonNode> event) {
        log.info("Processing STAFF_TRANSFERRED for staffCode={} from school={}",
                event.getData().path("staffCode").asText(),
                event.getData().path("fromSchoolCode").asText()
        );

        return consumedEventRepository
            .existsById(event.getEventId())
            .doOnSubscribe(s -> log.info("Subscribed to handle Mono for eventId: {}", event.getEventId()))
            .flatMap(
                alreadyProcessed -> {
                  if (Boolean.TRUE.equals(alreadyProcessed)) {
                    log.info("Duplicate transfer event ignored: {}", event.getEventId());
                    return Mono.empty();
                  }
                  StaffTransferredEvent eventData =  objectMapper.convertValue(event.getData(), StaffTransferredEvent.class);
                  return unassignStaffFromOldSchool(eventData)
                      .then(
                          consumedEventRepository.save(ConsumedEvent.builder()
                              .eventId(event.getEventId())
                              .eventType(event.getEventType())
                              .consumedAt(java.time.Instant.now())
                              .build()))
                          .then();
                })
            .doOnSuccess(v -> log.info("Successfully processed transfer event {}", event.getEventId()))
            .onErrorResume(
                ex -> {
                  log.error("Failed processing staff transfer event {}", event.getEventId(), ex);
                  return Mono.empty();
                })
            .doOnTerminate(() -> log.info("Terminated handle Mono for eventId: {}", event.getEventId()));
    }

    private Mono<Void> unassignStaffFromOldSchool(StaffTransferredEvent event) {

        return classSectionRepository
                .unassignStaffFromSchool(
                        event.getFromSchoolCode(),
                        event.getStaffId()
                )
                .doOnNext(count ->
                        log.info("Unassigned staff {} from {} sections in school {}",
                                event.getStaffCode(), count, event.getFromSchoolCode()))
                .as(transactionalOperator::transactional)
                .then();
    }
}