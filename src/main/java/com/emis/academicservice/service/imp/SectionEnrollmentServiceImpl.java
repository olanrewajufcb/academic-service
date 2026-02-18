package com.emis.academicservice.service.imp;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.domain.db.SectionEnrollment;
import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.SectionEnrollmentMapper;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.repository.SectionEnrollmentRepository;
import com.emis.academicservice.service.ActiveStudentEnrollmentCache;
import com.emis.academicservice.service.SectionEnrollmentService;
import com.emis.academicservice.service.client.StudentClientService;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
            validation ->
                studentEnrollmentCache
                        .getStudentEnrollmentFromCache(
                                request.getSchoolCode(),
                                request.getStudentNumber(),
                                request.getAcademicYear())
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
                        }))
        .as(transactionalOperator::transactional)
        .doOnSuccess(
            saved ->
                log.info(
                    "[{}] Student {} enrolled in section {}",
                    requestId,
                    saved.getStudentId(),
                    saved.getSectionId()))
        .map(mapper::toResponse)
        .onErrorMap(
            TimeoutException.class, ex -> new ExternalServiceException("Student service timeout"))
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex -> {
              if (ex.getMessage().contains("section_enrollment_section_id_student_id_key")) {
                return new AlreadyExistsException("Duplicate section enrollment");
              }
              return new EnrollmentFailureException("DB error", ex);
            });
    }
}
