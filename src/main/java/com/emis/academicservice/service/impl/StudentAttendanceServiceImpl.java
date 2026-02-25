package com.emis.academicservice.service.impl;

import com.emis.academicservice.domain.db.Lesson;
import com.emis.academicservice.domain.db.StudentAttendance;
import com.emis.academicservice.dto.request.StudentAttendanceList;
import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.SectionAttendanceReportResponse;
import com.emis.academicservice.dto.response.SectionStudentAttendanceSummary;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import com.emis.academicservice.dto.response.StudentAttendanceSummaryResponse;
import com.emis.academicservice.event.domain.AttendanceEvent;
import com.emis.academicservice.event.domain.DomainEvent;
import com.emis.academicservice.event.domain.OutboxEvent;
import com.emis.academicservice.exception.ResourceAlreadyExistsException;
import com.emis.academicservice.repository.*;
import com.emis.academicservice.service.StudentAttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceServiceImpl implements StudentAttendanceService {
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final TransactionalOperator transactionalOperator;
    private final OutboxEventRepository outboxEventRepository;
    private final SectionEnrollmentRepository sectionEnrollmentRepository;
    private final ObjectMapper objectMapper;
    private final LessonRepository lessonRepository;

    public Flux<StudentAttendanceResponse> markStudentAttendance(
            StudentAttendanceRequest request,
            String schoolCode,
            String requestId) {

        return lessonRepository
                .findById(request.lessonId())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Lesson not found")))
                .flatMapMany(lesson ->
                        Flux.fromIterable(request.studentList())
                                .flatMap(student ->
                                        validateAndProcessStudentAttendance(
                                                student,
                                                lesson,
                                                schoolCode
                                        )))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<StudentAttendanceSummaryResponse> computeStudentAttendanceSummary(String schoolCode, String studentNumber, Long termId, String requestId) {

        return studentAttendanceRepository
                .getAttendanceSummary(studentNumber, termId, schoolCode)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("No attendance data found")))
                .map(projection -> {

                    long total = projection.getTotalLessons() != null
                            ? projection.getTotalLessons() : 0;

                    long present = projection.getPresent() != null
                            ? projection.getPresent() : 0;

                    long absent = projection.getAbsent() != null
                            ? projection.getAbsent() : 0;

                    long late = projection.getLate() != null
                            ? projection.getLate() : 0;

                    double percentage = total == 0
                            ? 0.0
                            : ((double) present / total) * 100;

                    return new StudentAttendanceSummaryResponse(
                            studentNumber,
                            termId,
                            total,
                            present,
                            absent,
                            late,
                            Math.round(percentage * 100.0) / 100.0
                    );
                });
    }

    private Mono<StudentAttendanceResponse> validateAndProcessStudentAttendance(
            StudentAttendanceList student,
            Lesson lesson,
            String schoolCode
    ) {

        if (!lesson.getSchoolCode().equals(schoolCode)) {
            return Mono.error(new ResourceNotFoundException("Lesson not found in this school"));
        }

        return sectionEnrollmentRepository
                .findByStudentNumberAndSectionId(
                        student.studentNumber(),
                        lesson.getSectionId())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Student not enrolled in this section")))
                .flatMap(sectionEnrollment ->
                        registerStudentAttendance(
                            student,
                            lesson,
                            schoolCode,
                            sectionEnrollment.getStudentId())
                );
    }
    private Mono<StudentAttendanceResponse> registerStudentAttendance(
            StudentAttendanceList requestList,
            Lesson lesson,
            String schoolCode,
            Long studentId
    ) {
        StudentAttendance studentAttendance = StudentAttendance.builder()
                .lessonId(lesson.getLessonId())
                .studentId(studentId)
                .studentNumber(requestList.studentNumber())
                .studentName(requestList.studentName())
                .attendanceStatus(requestList.status().name())
                .schoolCode(schoolCode)
                .notes(requestList.notes())
                .build();

        return studentAttendanceRepository.save(studentAttendance)
                .flatMap(attendance ->
                        writeAttendanceOutboxEvent(
                                attendance,
                                requestList,
                                lesson,
                                schoolCode)
                                .thenReturn(attendance))
                .map(StudentAttendanceResponse::from)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new ResourceAlreadyExistsException("Student attendance already marked"));
    }

    private Mono<Void> writeAttendanceOutboxEvent(
            StudentAttendance studentAttendance,
            StudentAttendanceList student,
            Lesson lesson,
            String schoolCode
    ) {

        UUID correlationId = UUID.randomUUID();
        AttendanceEvent payload =
                AttendanceEvent.builder()
                        .attendanceId(studentAttendance.getAttendanceId())
                        .termId(lesson.getTermId())
                        .sectionId(lesson.getSectionId())
                        .studentNumber(student.studentNumber())
                        .schoolCode(schoolCode)
                        .lessonId(lesson.getLessonId())
                        .lessonDate(lesson.getLessonDate())
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
                        .topic("attendance-out-0")
                        .payload(objectMapper.valueToTree(event))
                        .status("PENDING")
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .build()
        ).then();
    }


    public Mono<SectionAttendanceReportResponse> getSectionAttendanceReport(
            String schoolCode,
            Long sectionId,
            Long termId,
            String requestId) {

        Mono<SectionAttendanceAggregateProjection> aggregate =
                studentAttendanceRepository.getSectionAggregate(sectionId, termId, schoolCode);

        Mono<SectionAttendanceStatsProjection> stats =
                studentAttendanceRepository.getSectionAttendanceStats(sectionId, termId, schoolCode);

        Mono<List<SectionStudentAttendanceSummary>> students =
                studentAttendanceRepository.getStudentBreakdown(sectionId, termId, schoolCode)
                        .map(p -> {
                            long total = p.getTotalLessons() != null ? p.getTotalLessons() : 0;
                            long present = p.getPresent() != null ? p.getPresent() : 0;

                            double percentage = total == 0
                                    ? 0
                                    : ((double) present / total) * 100;

                            return new SectionStudentAttendanceSummary(
                                    p.getStudentNumber(),
                                    total,
                                    present,
                                    Math.round(percentage * 100.0) / 100.0
                            );
                        })
                        .collectList();

        return Mono.zip(aggregate, stats, students)
                .map(tuple -> {

                    var agg = tuple.getT1();
                    var stat = tuple.getT2();
                    var studentList = tuple.getT3();

                    long totalLessons = agg.getTotalLessons() != null ? agg.getTotalLessons() : 0;
                    long lessonsWithAttendance = agg.getLessonsWithAttendance() != null
                            ? agg.getLessonsWithAttendance() : 0;
                    long lessonsWithoutAttendance = agg.getLessonsWithoutAttendance() != null
                            ? agg.getLessonsWithoutAttendance() : 0;

                    long totalRecords = stat.getTotalRecords() != null ? stat.getTotalRecords() : 0;
                    long present = stat.getPresent() != null ? stat.getPresent() : 0;

                    double averageRate = totalRecords == 0
                            ? 0
                            : ((double) present / totalRecords) * 100;

                    return new SectionAttendanceReportResponse(
                            sectionId,
                            termId,
                            totalLessons,
                            lessonsWithAttendance,
                            lessonsWithoutAttendance,
                            totalRecords,
                            Math.round(averageRate * 100.0) / 100.0,
                            studentList
                    );
                });
    }

}
