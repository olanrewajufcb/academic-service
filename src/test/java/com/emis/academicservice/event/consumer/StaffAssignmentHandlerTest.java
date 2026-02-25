package com.emis.academicservice.event.consumer;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.event.ConsumedEventRepository;
import com.emis.academicservice.event.domain.ConsumedEvent;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAssignmentHandlerTest {

    @Mock
    private ClassSectionRepository classSectionRepository;
    @Mock
    private ConsumedEventRepository consumedEventRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

    private ObjectMapper objectMapper = new ObjectMapper();
    private StaffAssignmentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StaffAssignmentHandler(
                classSectionRepository,
                consumedEventRepository,
                transactionalOperator,
                objectMapper
        );
        
        // Mock transactional operator to just return the same publisher
        lenient().when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testHandle_SuccessfulAssignment() {
        UUID eventId = UUID.randomUUID();
        ObjectNode data = objectMapper.createObjectNode();
        data.put("sectionId", 1L);
        data.put("staffId", 4L);
        data.put("staffCode", "ST3");
        data.put("staffName", "Wahab Dare");

        DomainEvent<JsonNode> event = DomainEvent.<JsonNode>builder()
                .eventId(eventId)
                .eventType("STAFF_ASSIGNED_TO_CLASS")
                .data(data)
                .build();

        ClassSection section = ClassSection.builder()
                .sectionId(1L)
                .teacherId(null)
                .build();

        when(consumedEventRepository.existsById(eventId)).thenReturn(Mono.just(false));
        when(classSectionRepository.findById(1L)).thenReturn(Mono.just(section));
        when(classSectionRepository.save(any(ClassSection.class))).thenReturn(Mono.just(section));
        when(consumedEventRepository.save(any(ConsumedEvent.class))).thenReturn(Mono.just(new ConsumedEvent()));

        StepVerifier.create(handler.handle(event))
                .verifyComplete();

        verify(classSectionRepository).save(argThat(s -> 
            Long.valueOf(4L).equals(s.getTeacherId()) && 
            "ST3".equals(s.getStaffCode()) && 
            "Wahab Dare".equals(s.getTeacherName())
        ));
        verify(consumedEventRepository).save(any(ConsumedEvent.class));
    }

    @Test
    void testHandle_AlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        DomainEvent<JsonNode> event = DomainEvent.<JsonNode>builder()
                .eventId(eventId)
                .build();

        when(consumedEventRepository.existsById(eventId)).thenReturn(Mono.just(true));

        StepVerifier.create(handler.handle(event))
                .verifyComplete();

        verify(classSectionRepository, never()).findById(anyLong());
        verify(consumedEventRepository, never()).save(any());
    }
}
