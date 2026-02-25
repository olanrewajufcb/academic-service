package com.emis.academicservice.event;

import com.emis.academicservice.event.domain.ConsumedEvent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;


public interface ConsumedEventRepository
        extends ReactiveCrudRepository<ConsumedEvent, UUID> {
}