package com.emis.academicservice.event.config;

import com.emis.academicservice.event.consumer.StaffAssignmentHandler;
import com.emis.academicservice.event.consumer.StaffTransferEventHandler;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.event.domain.StaffTransferredEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class HrEventRouter {

    private final StaffAssignmentHandler staffAssignedHandler;
    private final StaffTransferEventHandler staffTransferredHandler;

    public Mono<Void> route(DomainEvent<JsonNode> event) {
        return switch (event.getEventType()) {
            case "STAFF_ASSIGNED_TO_CLASS" -> staffAssignedHandler.handle(event);

            case "STAFF_TRANSFERRED" -> staffTransferredHandler.handle(event);

            default ->  {
                log.warn("Unhandled HR event type: {}", event.getEventType());
                yield Mono.empty();
            }
        };
    }
}