package com.emis.academicservice.event.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DomainEvent<T> {
    private UUID eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;
    private String producer;
    private UUID correlationId;
    private T data;
}