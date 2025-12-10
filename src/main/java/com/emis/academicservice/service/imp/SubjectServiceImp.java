package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import com.emis.academicservice.exception.SchoolClassCreationException;
import com.emis.academicservice.exception.SubjectAlreadyExistsException;
import com.emis.academicservice.exception.SubjectNotFoundException;
import com.emis.academicservice.mapper.SubjectMapper;
import com.emis.academicservice.repository.SubjectRepository;
import com.emis.academicservice.service.SubjectService;
import com.emis.academicservice.service.client.SchoolClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubjectServiceImp implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final SchoolCacheService schoolCacheService;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClientService schoolClientService;



    @Override
    public Mono<SubjectResponse> registerSubject(RegisterSubjectRequest request, String requestId) {
        Subject subject = subjectMapper.toEntity(request);
        String schoolCode = request.getSchoolCode();
        return schoolCacheService.getSchoolIdByCode(schoolCode)
                .flatMap(schoolId -> Mono.defer(() -> subjectRepository
                                .findBySchoolIdAndCode(schoolId, request.getSubjectCode()))
                        .flatMap(duplicatSubject ->
                                Mono.error(new SubjectAlreadyExistsException(
                                String.format("Subject %s already exists for school %s", request.getSubjectCode(), schoolCode))))
                        .then(Mono.defer(() -> subjectRepository.save(subject)))
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedClass -> log.info("School class created successfully with id {} | requestId: {}", savedClass.getSubjectId(), requestId))
                .map(subjectMapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex ->
                        new DataIntegrityViolationException("Data integrity violation: " + ex.getMessage(), ex))
                .onErrorResume(ex -> {
                    log.error("Failed to create school class | requestId {} | error {}", requestId, ex.getMessage());
                    return Mono.error(new SchoolClassCreationException("Failed to create school class " + requestId, ex));
                }));
    }

    public Flux<SubjectResponse> getSubjectBySchoolAndClassLevel(String schoolCode, String classLevel, Pageable pageable, String requestId) {

    return schoolCacheService
        .getSchoolIdByCode(schoolCode)
        .flatMapMany(
            schoolId ->
                subjectRepository.findBySchoolIdAndClassLevel(
                    schoolId, classLevel, pageable.getPageSize(), pageable.getOffset()))
        .switchIfEmpty(
            Mono.error(
                new SubjectNotFoundException(
                    String.format(
                        "Subject %s not found for %s with requestId %s",
                        schoolCode, classLevel, requestId))))
        .map(subjectMapper::toResponse);
    }
}
