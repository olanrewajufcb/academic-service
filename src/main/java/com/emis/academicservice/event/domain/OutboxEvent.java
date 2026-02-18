package com.emis.academicservice.event.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(schema = "hr_schema", name = "outbox_events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent {

    @Id
    private Long outboxId;

    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String topic;

    @Column("payload")
    private JsonNode payload;

    private String status;
    private Integer retryCount;
    private Instant createdAt;
    private Instant publishedAt;
}