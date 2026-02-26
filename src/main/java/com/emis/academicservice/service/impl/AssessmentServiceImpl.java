package com.emis.academicservice.service.impl;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.enums.AssessmentType;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.AssessmentMapper;
import com.emis.academicservice.repository.AssessmentRepository;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;


@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolCacheService schoolCacheService;
    private final ClassSectionRepository classSectionRepository;

    @Override
    public Mono<AssessmentResponse> createAssessment(CreateAssessmentRequest request,
                                                    String schoolCode, String requestId) {

    return classSectionRepository
        .findBySectionIdAndSchoolCode(request.getSectionId(), schoolCode)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Class section not found")))
        .flatMap(
            classSection -> {
              Assessment assessment = mapper.toEntity(request);
              assessment.setSchoolId(classSection.getSchoolId());
              assessment.setTermId(classSection.getTermId());
              assessment.setSchoolCode(classSection.getSchoolCode());
              return assessmentRepository.save(assessment);
            })
        .as(transactionalOperator::transactional)
        .doOnNext(
            assessment ->
                log.info(
                    "[{}] Assessment created: id={}, sectionId={}, termId={}",
                    requestId,
                    assessment.getAssessmentId(),
                    assessment.getSectionId(),
                    assessment.getTermId()))
        .map(mapper::toResponse)
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex -> {
              if (ex.getMessage().contains("uk_section_term_name")) {
                return new DuplicateAssessmentException(
                    "Assessment '" + request.getName() + "' already exists", ex);
              } else if (ex.getMessage().contains("assessments_max_score_check")) {
                return new InvalidAssessmentException("maxScore must be > 0", ex);
              }
              return new AssessmentCreationException("Failed to create assessment", ex);
            });
    }

    @Override
    public Mono<Page<AssessmentResponse>> getAllAssessmentsForClassSection(Long sectionId, String schoolCode,
                              AssessmentType assessmentType, String term, Pageable pageable, String requestId) {
        int size = pageable.getPageSize();
        long offset = pageable.getOffset();

    return schoolCacheService
        .getSchoolIdByCode(schoolCode)
        .flatMap(
            schoolId ->
                classSectionRepository
                    .findBySectionIdAndSchoolId(sectionId, schoolId)
                    .switchIfEmpty(
                        Mono.error(
                            new ResourceNotFoundException(
                                String.format(
                                    "Section %d not found in school %s", sectionId, schoolCode))))
                    .flatMap(
                        classSection ->
                            Mono.zip(
                                assessmentRepository
                                    .findBySectionIdAndSchoolId(
                                        sectionId,
                                        schoolId,
                                        assessmentType.name(),
                                        term,
                                        size,
                                        offset)
                                    .collectList(),
                                assessmentRepository.countBySectionIdAndSchoolId(
                                    sectionId, schoolId, assessmentType.name(), term))))
        .timeout(Duration.ofSeconds(3))
        .flatMap(
            tuple -> {
              List<Assessment> assessments = tuple.getT1();
              long total = tuple.getT2();

              if (total == 0) {
                Page<AssessmentResponse> emptyPage = Page.empty();
                return Mono.just(emptyPage);
              }
              List<AssessmentResponse> responseList =
                  assessments.stream().map(mapper::toResponse).toList();
              return Mono.just(new PageImpl<>(responseList, pageable, total));
            })
        .doOnSuccess(
            page ->
                log.info(
                    "[{}] Successfully fetched {} assessments for section {}",
                    requestId,
                    page.getTotalElements(),
                    sectionId))
        .onErrorMap(
            ex -> {
              if (ex instanceof TimeoutException) {
                log.error("[{}] Timeout error fetching assessments", requestId);
                return new TimeoutException("Timeout error occurred");
              } else if (ex instanceof DataAccessException err) {
                log.error("[{}] Error fetching assessments::::", requestId, err);
                return new AssessmentServiceException("Error occurred", err);
              }
              return new AssessmentServiceException("Database error occurred", ex);
            });
    }
}
