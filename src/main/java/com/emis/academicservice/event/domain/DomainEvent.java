package com.emis.academicservice.event.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DomainEvent<T> {
    private String eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;
    private String producer;
    private String correlationId;
    private T data;
}