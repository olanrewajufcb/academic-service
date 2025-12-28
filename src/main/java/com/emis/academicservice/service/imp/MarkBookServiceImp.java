package com.emis.academicservice.service.imp;


import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.domain.db.MarkBookEntry;
import com.emis.academicservice.dto.request.RecordAssessmentRequest;
import com.emis.academicservice.dto.response.*;
import com.emis.academicservice.enums.AssessmentStatus;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.MarkBookMapper;
import com.emis.academicservice.repository.AssessmentRepository;
import com.emis.academicservice.repository.MarkBookEntryRepository;
import com.emis.academicservice.repository.SectionEnrollmentRepository;
import com.emis.academicservice.service.MarkBookService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service
public class MarkBookServiceImp implements MarkBookService {

    private final SchoolCacheService schoolCacheService;
    private final AssessmentRepository assessmentRepository;
    private final MarkBookEntryRepository markBookEntryRepository;
    private final StudentClientService studentClientService;
    private final SectionEnrollmentRepository sectionEnrollmentRepository;
    private final TransactionalOperator transactionOperator;
    private final MarkBookMapper markBookMapper;

    @Override
    public Mono<MarkBookResponse> recordAssessmentMark(RecordAssessmentRequest request, String requestId) {
        if (request == null || request.scoreObtained() == null || request.studentNumber() == null || request.schoolCode() == null) {
            return Mono.error(new IllegalArgumentException("Invalid input parameters"));
        }

        return schoolCacheService.getSchoolIdByCode(request.schoolCode())
                .switchIfEmpty(Mono.error(new SchoolNotFoundException("School not found for " + request.schoolCode())))
                .flatMap(schoolId -> assessmentRepository
                        .findByAssessmentIdAndSchoolId(request.assessmentId(), schoolId)
                        .switchIfEmpty(Mono.error(new AssessmentNotFoundException(
                                "Assessment not found or doesn't belong to school " + request.assessmentId())))
                        .flatMap(assessment -> validateAssessmentForMarking(assessment, requestId))
                        .flatMap(assessment -> studentClientService.getStudentDetails(request.studentNumber(), request.schoolCode())
                                .switchIfEmpty(Mono.error(new StudentNotFoundException("Student not found for: " + request.studentNumber())))
                                .flatMap(student -> validateStudentEnrollment(student, assessment, requestId))
                                .flatMap(student -> {
                                    MarkBookEntry markbookEntry = new MarkBookEntry();
                                    markbookEntry.setAssessmentId(request.assessmentId());
                                    markbookEntry.setStudentId(student.studentId());
                                    markbookEntry.setScoreObtained(request.scoreObtained());
                                    markbookEntry.setRemark(request.remark());
                                    markbookEntry.setMarkedAt(LocalDateTime.now());
                                    markbookEntry.setCreatedAt(LocalDateTime.now());

                                    // Safely calculate percentage
                                    BigDecimal percentage = calculatePercentage(request.scoreObtained(), assessment.getMaxScore());
                                    markbookEntry.setScorePercentage(percentage);

                                    return markBookEntryRepository.save(markbookEntry)
                                            .as(transactionOperator::transactional)
                                            .onErrorMap(DataIntegrityViolationException.class, ex -> {
                                                if (ex.getMessage().contains("markbook_entry_assessment_id_student_id")) {
                                                    return new DuplicateClassException("Duplicate mark book entry");
                                                }
                                                return new MarkBookFailureException("DB error", ex);
                                            })
                                            .onErrorResume(ex -> {
                                                log.error("[{}] MarkBook entry failed for studentId = {}", requestId, ex);
                                                return Mono.error(ex);
                                            })
                                            .map(entry -> markBookMapper.toMarkBookResponse(entry, assessment, student)); // deferred mapping
                                })));
    }

    @Override
    public Mono<MarkBookViewResponse> getSectionMarkBook(Long sectionId, String schoolCode, Long assessmentId, String academicYear, String requestId) {
        return schoolCacheService.getSchoolIdByCode(schoolCode)
                .switchIfEmpty(Mono.error(new SchoolNotFoundException("School not found: " + schoolCode)))
                .flatMap(schoolId -> validateAssessmentBelongsToSchool(assessmentId, schoolId))
                .flatMap(assessment ->
                        markBookEntryRepository.findBySectionAndAssessmentAndAcademicYear(sectionId, assessmentId, academicYear)
                                .collectList()
                                .flatMap(entries -> enrichEntriesWithStudentDetails(entries, schoolCode))
                                .flatMap(enrichedEntries ->
                                        Mono.zip(Mono.just(enrichedEntries),
                                                calculateMarkBookStatistics(enrichedEntries))

                )   .map(tuple -> {
                    List<MarkBookEnrichedEntry> entries = tuple.getT1();
                    MarkBookStatistics stats = tuple.getT2();
                    return buildMarkBookViewResponse(
                            sectionId, assessmentId, academicYear, entries, stats, assessment
                    );
                })
        )
                .doOnSuccess(response ->
                        log.info("[{}] Retrieved markbook with {} entries for section {}",
                                requestId, response.marks().size(), sectionId)
                )
                .onErrorMap(ex -> {
                    log.error("[{}] Failed to retrieve markbook for section {}",
                            requestId, sectionId, ex);
                    return new MarkBookFailureException(
                            "Failed to retrieve markbook for section " + sectionId, ex);
                });
    }
    private Mono<Assessment> validateAssessmentBelongsToSchool(Long assessmentId, Long schoolId) {
        return assessmentRepository.findByAssessmentIdAndSchoolId(assessmentId, schoolId)
                .switchIfEmpty(Mono.error(new AssessmentNotFoundException(
                        "Assessment " + assessmentId + " not found or doesn't belong to school")));
    }

    private BigDecimal calculatePercentage(BigDecimal scoreObtained, BigDecimal maxScore) {
        if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return scoreObtained.divide(maxScore, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }



    private Mono<Assessment> validateAssessmentForMarking(Assessment assessment, String requestId) {
        // Check if assessment is still open for marking
        if (LocalDate.now().isAfter(assessment.getDueDate().plusDays(7))) {
            log.warn("[{}] Attempted to mark overdue assessment: {}",
                    requestId, assessment.getAssessmentId());
            return Mono.error(new AssessmentClosedException(
                    "Assessment is closed for marking (overdue by more than 7 days)"));
        }

        // Check if assessment is completed
        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            log.warn("[{}] Attempted to mark completed assessment: {}",
                    requestId, assessment.getAssessmentId());
            return Mono.error(new AssessmentClosedException(
                    "Assessment is already marked as completed"));
        }

        return Mono.just(assessment);
    }

    private Mono<StudentDetailsResponse> validateStudentEnrollment(
            StudentDetailsResponse student, Assessment assessment, String requestId) {

        // Check if student is enrolled in the section
        return sectionEnrollmentRepository.existsByStudentIdAndSectionId(
                        student.studentId(), assessment.getSectionId())
                .flatMap(enrolled -> {
                    if (Boolean.FALSE.equals(enrolled)) {
                        log.warn("[{}] Student {} not enrolled in assessment section",
                                requestId, student.studentId());
                        return Mono.error(new StudentNotEnrolledException(
                                "Student is not enrolled in this assessment's class section"));
                    }
                    return Mono.just(student);
                });
    }

    private Mono<List<MarkBookEnrichedEntry>> enrichEntriesWithStudentDetails(
            List<MarkBookEntryDetail> entries, String schoolCode
    ) {
        if (entries.isEmpty()) {
            return Mono.just(List.of());
        }

        // Get student details for all entries
        return Flux.fromIterable(entries)
                .flatMap(entry ->
                        studentClientService.getStudentByIdAndSchoolId(entry.studentId(), schoolCode)
                                .map(student -> new MarkBookEnrichedEntry(
                                        entry,
                                        student.studentNumber(),
                                        student.fullName(),
                                        student.gradeLevel()
                                ))
                                .onErrorResume(e -> {
                                    log.warn("Could not fetch student {} details: {}", entry.studentId(), e.getMessage());
                                    return Mono.just(new MarkBookEnrichedEntry(
                                            entry,
                                            "N/A",
                                            "Unknown Student",
                                            "N/A"
                                    ));
                                }), 32
                )
                .collectList();
    }

    private Mono<MarkBookStatistics> calculateMarkBookStatistics(List<MarkBookEnrichedEntry> entries) {
        if (entries.isEmpty()) {
            return Mono.just(MarkBookStatistics.empty());
        }

        return Mono.fromCallable(() -> {
            List<BigDecimal> scores = entries.stream()
                    .filter(e -> e.entry().scoreObtained() != null)
                    .map(e -> e.entry().scoreObtained())
                    .toList();

            if (scores.isEmpty()) {
                return MarkBookStatistics.empty();
            }

            // Calculate statistics
            BigDecimal total = scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = total.divide(
                    new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);

            BigDecimal highest = scores.stream()
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal lowest = scores.stream()
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            int graded = scores.size();
            int totalStudents = entries.size();

            // Grade distribution (Nigerian system: A=70+, B=60-69, C=50-59, D=45-49, F=<45)
            Map<String, Long> gradeDistribution = entries.stream()
                    .filter(e -> e.entry().scorePercentage() != null)
                    .collect(Collectors.groupingBy(
                            e -> {
                                BigDecimal percentage = e.entry().scorePercentage();
                                if (percentage.compareTo(new BigDecimal("70")) >= 0) return "A";
                                if (percentage.compareTo(new BigDecimal("60")) >= 0) return "B";
                                if (percentage.compareTo(new BigDecimal("50")) >= 0) return "C";
                                if (percentage.compareTo(new BigDecimal("45")) >= 0) return "D";
                                return "F";
                            },
                            Collectors.counting()
                    ));

            return new MarkBookStatistics(
                    average,
                    highest,
                    lowest,
                    graded,
                    totalStudents,
                    totalStudents - graded, // pending
                    gradeDistribution
            );
        }).subscribeOn(Schedulers.parallel());
    }

    private MarkBookViewResponse buildMarkBookViewResponse(
            Long sectionId,
            Long assessmentId,
            String academicYear,
            List<MarkBookEnrichedEntry> entries,
            MarkBookStatistics statistics,
            Assessment assessment) {
        return new MarkBookViewResponse(
                sectionId,
                assessmentId,
                assessment.getName(),
                academicYear,
                entries.stream()
                        .map(e -> new MarkEntryDetail(
                                e.entry().studentId(),
                                e.studentNumber(),
                                e.studentName(),
                                e.entry().subjectName(),
                                e.entry().scoreObtained(),
                                e.entry().maxScore(),
                                e.entry().scorePercentage(),
                                e.entry().remark(),
                                e.entry().markedAt(),
                                ""
                        ))
                        .sorted(Comparator.comparing(MarkEntryDetail::studentName))
                        .toList(),
                statistics.averageScore(),
                statistics.highestScore(),
                statistics.lowestScore(),
                statistics.totalStudents(),
                statistics.gradedStudents(),
                statistics.pendingGrading(),
                LocalDateTime.now()
        );
    }
}
