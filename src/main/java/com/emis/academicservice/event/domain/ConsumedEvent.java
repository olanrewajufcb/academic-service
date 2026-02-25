package com.emis.academicservice.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Transient;

import java.time.Instant;
import java.util.UUID;

@Table(schema = "academic_schema", name = "consumed_events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumedEvent implements Persistable<UUID> {

    @Id
    private UUID eventId;
    private String eventType;
    private Instant consumedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}