package com.emis.academicservice.service.impl;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.domain.db.SectionEnrollment;
import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import com.emis.academicservice.dto.response.SubjectDto;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.helper.ReactivePageSupport;
import com.emis.academicservice.mapper.SectionEnrollmentMapper;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.repository.SectionEnrollmentRepository;
import com.emis.academicservice.service.ActiveStudentEnrollmentCache;
import com.emis.academicservice.service.SectionEnrollmentService;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class SectionEnrollmentServiceImpl implements SectionEnrollmentService {

    private final ClassSectionRepository classSectionRepository;
    private final SectionEnrollmentRepository sectionEnrollmentRepository;
    private final TransactionalOperator transactionalOperator;
    private final ActiveStudentEnrollmentCache studentEnrollmentCache;
    private final SectionEnrollmentMapper mapper;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<SectionEnrollmentResponse> enrollStudentInClassSection(Long sectionId,
                                         EnrollStudentInClassSectionRequest request, String requestId) {

    return classSectionRepository
        .validateSectionOwnership(sectionId, request.getSchoolCode())
        .switchIfEmpty(
            Mono.error(
                new InvalidEnrollmentException(
                    "Section "
                        + sectionId
                        + " not found for school '"
                        + request.getSchoolCode()
                        + "'")))
        .flatMap(
            validation -> {
              log.info(
                  "[{}] Validated section id {} and academic year for school '{}'",
                  requestId,
                  validation.getSectionId(),
                  validation.getAcademicYear());
              return studentEnrollmentCache
                  .getStudentEnrollmentFromCache(
                      request.getSchoolCode(),
                      request.getStudentNumber(),
                      validation.getAcademicYear())
                  .timeout(Duration.ofSeconds(properties.getTimeout()))
                  .flatMap(
                      student -> {
                        if (!request.getSchoolCode().equals(student.schoolCode())) {
                          return Mono.error(
                              new InvalidEnrollmentException(
                                  String.format(
                                      "Student %s belongs to school '%s', not '%s'",
                                      request.getStudentNumber(),
                                      student.schoolCode(),
                                      request.getSchoolCode())));
                        }

                        SectionEnrollment enrollment = mapper.toEntity(request);
                        enrollment.setSectionId(sectionId);
                        enrollment.setStudentId(student.studentId());
                        enrollment.setStudentNumber(student.studentNumber());
                        enrollment.setEnrollmentDate(LocalDate.now());
                        return sectionEnrollmentRepository.save(enrollment);
                      });
            })
        .as(transactionalOperator::transactional)
        .doOnSuccess(
            saved ->
                log.info(
                    "[{}] Student {} enrolled in section {}",
                    requestId,
                    saved.getStudentId(),
                    saved.getSectionId()))
        .map(SectionEnrollmentResponse::from)
        .onErrorMap(
            TimeoutException.class, ex -> new ExternalServiceException("Student service timeout"))
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex -> {
              if (ex.getMessage().contains("section_enrollment_section_id_student_id_key")) {
                return new ResourceNotFoundException(
                    "Duplicate section enrollment");
              }
              return new EnrollmentFailureException("DB error", ex);
            });
    }

    @Override
    public Mono<Page<SubjectDto>> getAllClassSections(
            String schoolCode,
            String studentNumber,
            Pageable pageable,
            String requestId) {

        return ReactivePageSupport.createPage(
            sectionEnrollmentRepository
            .findAllClassSectionsBySchoolAndStudent(
                    schoolCode,
                    studentNumber,
                    pageable.getPageSize(),
                    pageable.getOffset()),
            sectionEnrollmentRepository.countAllClassSectionsBySchoolAndStudent(
                    schoolCode,
                    studentNumber),
            pageable,
            SubjectDto::from,
            Duration.ofSeconds(properties.getTimeout())

        )
         .doOnSuccess(page ->
                        log.info("[{}] Retrieved {} subjects (page {}/{})",
                                requestId,
                                page.getNumberOfElements(),
                                page.getNumber() + 1,
                                page.getTotalPages()))
        .onErrorMap(
            error -> {
              log.error(
                  "[{}] Failed to fetch classes for classId: {}", requestId, schoolCode, error);
              if (error instanceof java.util.concurrent.TimeoutException) {
                return new DatabaseTimeoutException("Database operation timed out:::", error);
              }
              return new EnrollmentFailureException(
                  "Failed to fetch classes for schoolCode: "  + schoolCode, error);
            });
    }

    @Override
    public Mono<SubjectDto> removeStudentFromSection(Long sectionId, Long studentId) {
            return sectionEnrollmentRepository
                                    .softDeleteByStudentNumberAndSectionId(studentId, sectionId)
                    .flatMap(rows -> {
                        if (rows == 0) {
                            return Mono.error(
                                    new ResourceNotFoundException(
                                            String.format("Student %s not enrolled in section", sectionId)));
                        }
                        log.info("Student {} removed from section {}", studentId, sectionId);
                        return Mono.empty();
                    });

    }

}
