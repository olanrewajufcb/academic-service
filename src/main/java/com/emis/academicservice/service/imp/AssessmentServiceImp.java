package com.emis.academicservice.service.imp;

import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
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
        var assessment = mapper.toEntity(request);

        return Mono.defer(() -> repository.save(assessment))
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedEntity -> log.info("Successfully created assessment with id " + requestId))
                .map(mapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex ->
                        new DataIntegrityViolationException("Data integrity violation: " + ex.getMessage(), ex))
                .onErrorResume(ex -> {
                    log.error("Failed to create school class | requestId {} | error {}", requestId, ex.getMessage());
                    return Mono.error(new SchoolClassCreationException("Failed to create school class " + requestId, ex));
                });
    }
}
