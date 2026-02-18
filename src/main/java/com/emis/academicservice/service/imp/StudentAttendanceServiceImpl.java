package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.domain.db.StudentAttendance;
import com.emis.academicservice.dto.request.StudentAttendanceList;
import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import com.emis.academicservice.event.domain.AttendanceEvent;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.event.domain.OutboxEvent;
import com.emis.academicservice.exception.ClassSectionNotFoundException;
import com.emis.academicservice.exception.ResourceAlreadyExistsException;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.repository.OutboxEventRepository;
import com.emis.academicservice.repository.SectionEnrollmentRepository;
import com.emis.academicservice.repository.StudentAttendanceRepository;
import com.emis.academicservice.service.StudentAttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceServiceImpl implements StudentAttendanceService {
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final ClassSectionRepository classSectionRepository;
    private final TransactionalOperator transactionalOperator;
    private final OutboxEventRepository outboxEventRepository;
    private final SectionEnrollmentRepository sectionEnrollmentRepository;
    private final ObjectMapper objectMapper;

    public Flux<StudentAttendanceResponse> markStudentAttendance(
            StudentAttendanceRequest request, String schoolCode, String requestId) {

        return classSectionRepository.findBySectionIdAndSchoolCode(request.sectionId(), schoolCode)
                .switchIfEmpty(Mono.error(new ClassSectionNotFoundException(
                        "Class section with id " + request.sectionId() + " not found")))
                .flatMapMany(section ->
                        Flux.fromIterable(request.studentList())
                                .flatMap(student ->
                                        validateAndProcessStudentAttendance(student, section, request, schoolCode, requestId))
                );
    }

    private Mono<Void> writeAttendanceOutboxEvent(
            StudentAttendance studentAttendance,
            StudentAttendanceList student,
            StudentAttendanceRequest request,
            String schoolCode,
            String correlationId
    ) {

        AttendanceEvent payload =
                AttendanceEvent.builder()
                        .attendanceId(studentAttendance.getAttendanceId())
                        .studentNumber(student.studentNumber())
                        .schoolCode(schoolCode)
                        .sectionId(request.sectionId())
                        .attendanceDate(request.attendanceDate())
                        .attendanceStatus(student.status().name())
                        .notes(student.notes())
                        .correlationId(correlationId)
                        .build();

        DomainEvent<AttendanceEvent> event =
                DomainEvent.<AttendanceEvent>builder()
                        .eventId(correlationId)
                        .eventType("ATTENDANCE_EVENT")
                        .eventVersion(1)
                        .occurredAt(Instant.now())
                        .producer("academic-service")
                        .correlationId(correlationId)
                        .data(payload)
                        .build();

        return outboxEventRepository.save(
                OutboxEvent.builder()
                        .eventId(event.getEventId())
                        .aggregateType("STUDENT")
                        .aggregateId(student.studentNumber())
                        .eventType(event.getEventType())
                        .topic("attendance.out.0")
                        .payload(objectMapper.valueToTree(event))
                        .status("PENDING")
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .build()
        ).then();
    }



    private Mono<StudentAttendanceResponse> validateAndProcessStudentAttendance(
            StudentAttendanceList student, ClassSection section,
            StudentAttendanceRequest request, String schoolCode, String requestId) {

        return Mono.zip(
                        sectionEnrollmentRepository
                                .existsByStudentNumberAndSectionId(student.studentNumber(), section.getSectionId()),
                        studentAttendanceRepository
                                .findBySectionIdAndStudentNumberAndAttendanceDate(
                                        section.getSectionId(),
                                        student.studentNumber(),
                                        request.attendanceDate())
                )
                .flatMap(tuple -> {
                    Boolean studentExistsInSection = tuple.getT1();
                    StudentAttendance existingAttendance = tuple.getT2();

                    if (!studentExistsInSection) {
                        return Mono.error(new ResourceNotFoundException("Student does not exist in the given subject group"));
                    }
                    if (existingAttendance != null) {
                        return Mono.error(new ResourceAlreadyExistsException("Student already marked attendance"));
                    }

                    return registerStudentAttendance(student, section, request, schoolCode, requestId);
                });
    }
    private Mono<StudentAttendanceResponse> registerStudentAttendance(
            StudentAttendanceList requestList, ClassSection section,
            StudentAttendanceRequest attendanceRequest, String schoolCode,
            String requestId) {
        StudentAttendance studentAttendance = StudentAttendance.builder()
                .sectionId(section.getSectionId())
                .studentNumber(requestList.studentNumber())
                .attendanceStatus(requestList.status().name())
                .notes(requestList.notes())
                .build();
        return studentAttendanceRepository.save(studentAttendance)
                .flatMap(attendance ->
                        writeAttendanceOutboxEvent(attendance, requestList, attendanceRequest,
                                schoolCode, requestId)
                                .thenReturn(attendance))
                .as(transactionalOperator::transactional)
                .map(StudentAttendanceResponse::from);
    }
}
