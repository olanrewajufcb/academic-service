package com.emis.academicservice.event.publisher;

import com.emis.academicservice.event.domain.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentAttendanceEventPublisher {

    private final StreamBridge streamBridge;

    public Mono<Void> publish(OutboxEvent outboxEvent) {
        return Mono.fromCallable(() -> {
            boolean sent =
                streamBridge.send(
                    outboxEvent.getTopic(),
                    MessageBuilder
                        .withPayload(outboxEvent.getPayload())
                        .setHeader("eventId", outboxEvent.getEventId())
                        .setHeader("aggregateId", outboxEvent.getAggregateId())
                        .setHeader("eventType", outboxEvent.getEventType())
                        .build()
                );
            log.info("Sending to binding {} result: {}", outboxEvent.getTopic(), sent);

            if (!sent) {
                throw new IllegalStateException("Failed to send Kafka message");
            }

            return true;
        }).then().subscribeOn(Schedulers.boundedElastic());
    }
}