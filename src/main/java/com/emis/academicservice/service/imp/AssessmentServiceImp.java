package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.exception.AssessmentCreationException;
import com.emis.academicservice.exception.DuplicateAssessmentException;
import com.emis.academicservice.exception.InvalidAssessmentException;
import com.emis.academicservice.exception.SchoolClassCreationException;
import com.emis.academicservice.mapper.AssessmentMapper;
import com.emis.academicservice.repository.AssessmentRepository;
import com.emis.academicservice.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImp implements AssessmentService {
    private final AssessmentRepository repository;
    private final AssessmentMapper mapper;
    private final TransactionalOperator transactionalOperator;
    @Override
    public Mono<AssessmentResponse> createAssessment(CreateAssessmentRequest request, String requestId) {

        return transactionalOperator.execute(status ->
                Mono.defer(() -> {
                    Assessment assessment = mapper.toEntity(request);
                    return repository.save(assessment);
                }))
                .next()
                .doOnNext(assessment -> log.info("[{}] Assessment created: id={}, sectionId={}, termId={}",
                                requestId, assessment.getAssessmentId(), assessment.getSectionId(), assessment.getTermId()))
                .map(mapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    if (ex.getMessage().contains("uk_section_term_name")) {
                        return new DuplicateAssessmentException(
                                "Assessment '" + request.getName() + "' already exists", ex);
                    } else if (ex.getMessage().contains("assessments_max_score_check")) {
                        return new InvalidAssessmentException("maxScore must be > 0",ex);
                    }
                    return new AssessmentCreationException("Failed to create assessment", ex);
                })
                .onErrorResume(ex -> {
                    log.error("[{}] Assessment creation failed", requestId, ex);
                    return Mono.error(ex);
                });
    }
}
