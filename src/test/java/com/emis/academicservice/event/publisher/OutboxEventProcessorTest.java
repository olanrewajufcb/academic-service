package com.emis.academicservice.event.publisher;

import com.emis.academicservice.event.domain.OutboxEvent;
import com.emis.academicservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxRepository;
    @Mock
    private StudentAttendanceEventPublisher publisher;

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxEventProcessor(outboxRepository, publisher);
    }

    @Test
    void testProcess_SuccessfulPublish() {
        OutboxEvent event = OutboxEvent.builder()
                .outboxId(1L)
                .eventId(UUID.randomUUID())
                .topic("attendance-out-0")
                .status("PENDING")
                .retryCount(0)
                .build();

        when(outboxRepository.findPending(50)).thenReturn(Flux.just(event));
        when(publisher.publish(event)).thenReturn(Mono.empty());
        when(outboxRepository.markSent(1L)).thenReturn(Mono.empty());

        processor.process();

        verify(publisher).publish(event);
        verify(outboxRepository).markSent(1L);
    }
}
