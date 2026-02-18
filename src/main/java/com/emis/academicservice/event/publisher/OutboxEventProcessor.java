package com.emis.academicservice.event.publisher;

import com.emis.academicservice.event.domain.OutboxEvent;
import com.emis.academicservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

  private final OutboxEventRepository outboxRepository;
  private final StudentAttendanceEventPublisher publisher;
  private static final int MAX_RETRIES = 5;

  @Scheduled(fixedDelay = 3000)
  public void process() {
    outboxRepository
        .findPending(50)
        .concatMap(this::publishSafely)
        .onErrorContinue((ex, obj) -> log.error("Error processing outbox event", ex))
        .subscribe();
  }

  private Mono<Void> publishSafely(OutboxEvent event) {
    return publisher
        .publish(event)
        .then(outboxRepository.markSent(event.getOutboxId()))
        .doOnSuccess(
            v -> log.info("Published event {} to topic {}", event.getEventId(), event.getTopic()))
        .onErrorResume(
            ex -> {
              log.error("Failed to publish event {}", event.getEventId(), ex);
              if (isNonRecoverable(ex)) {
                return outboxRepository.markSent(event.getOutboxId());
              }
              if (event.getRetryCount() + 1 >= MAX_RETRIES) {
                log.error("Event {} exceeded max retries, marking FAILED", event.getEventId());
                return outboxRepository.markFailed(event.getOutboxId());
              }
              return outboxRepository.incrementRetry(event.getOutboxId());
            });
  }

  private boolean isNonRecoverable(Throwable ex) {
    return ex instanceof IllegalArgumentException
        || ex instanceof SerializationException
        || ex instanceof JsonProcessingException;
  }
}
