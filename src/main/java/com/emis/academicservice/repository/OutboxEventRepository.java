package com.emis.academicservice.repository;

import com.emis.academicservice.event.domain.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxEventRepository
        extends ReactiveCrudRepository<OutboxEvent, Long> {

    @Query("""
        SELECT * FROM academic_schema.outbox_events
        WHERE status = 'PENDING'
        ORDER BY created_at
        LIMIT $1
        """)
    Flux<OutboxEvent> findPending(int limit);

    @Query("""
        UPDATE academic_schema.outbox_events
        SET status = 'SENT', published_at = NOW()
        WHERE outbox_id = :id
        """)
    Mono<Void> markSent(Long id);

    @Query("""
        UPDATE academic_schema.outbox_events
        SET retry_count = retry_count + 1
        WHERE outbox_id = :id
        """)
    Mono<Void> incrementRetry(Long id);

    @Query("""
    UPDATE academic_schema.outbox_events
    SET status = 'FAILED', published_at = NOW()
    WHERE outbox_id = $1
""")
    Mono<Void> markFailed(Long id);
}