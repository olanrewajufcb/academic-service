package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.SubjectMapper;
import com.emis.academicservice.repository.SubjectRepository;
import com.emis.academicservice.service.SubjectService;
import com.emis.academicservice.service.client.SchoolClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
        String schoolCode = request.getSchoolCode();
    return schoolCacheService
        .getSchoolIdByCode(schoolCode)
        .switchIfEmpty(Mono.error(new SchoolNotFoundException("School not found for " + schoolCode)))
        .flatMap(
            schoolId -> {
                          Subject subject = subjectMapper.toEntity(request);
                          subject.setSchoolId(schoolId);
                          return Mono.defer(() ->  subjectRepository.save(subject))
                                  .as(transactionalOperator::transactional)
                                  .doOnSuccess(savedSubject ->
                                          log.info("School class created successfully with id {} | requestId: {}",
                                                          savedSubject.getSubjectId(),
                                                          requestId))
                                  .map(subjectMapper::toResponse);

                        })

            .onErrorMap(DataIntegrityViolationException.class, ex -> {
                if (ex.getMessage() != null &&
                        ex.getMessage().contains("uk_school_subject_code")) {
                    return new SubjectAlreadyExistsException(
                            String.format("Subject '%s' already exists for school '%s'",
                                    request.getSubjectCode(), request.getSchoolCode()));
                }
                return new DataIntegrityViolationException("Database error", ex);
            })
            .onErrorResume(ex -> {
                log.error("[{}] Failed to register subject", requestId, ex);
                return Mono.error(ex);
                        });
    }

    public Mono<Page<SubjectResponse>> getSubjectBySchoolAndClassLevel(String schoolCode, String classLevel, Pageable pageable, String requestId) {

    return schoolCacheService
        .getSchoolIdByCode(schoolCode)
            .switchIfEmpty(Mono.error(new SchoolNotFoundException("School not found for " + schoolCode)))
        .flatMap(schoolId -> Mono.zip(
                subjectRepository.findBySchoolIdAndClassLevel(
                    schoolId, classLevel, pageable.getPageSize(), pageable.getOffset()).collectList(),
                    subjectRepository.countBySchoolIdAndClassLevel(schoolId, classLevel))
                .timeout(Duration.ofSeconds(3))

                            .flatMap(tuple -> {
                                List<Subject> subjects = tuple.getT1();
                                long total = tuple.getT2();
                                if (total == 0) {
                                    Page<SubjectResponse> page = Page.empty();
                                    return Mono.just(page);
                                }
                                List<SubjectResponse> responses = subjects.stream().map(subjectMapper::toResponse)
                                        .toList();
                                Page<SubjectResponse> page = new PageImpl<>(responses, pageable, total);
                                return Mono.just(page);
                            }))
            .onErrorMap(TimeoutException.class,
                    ex -> new SchoolClassFailureException("Database timeout", ex))
            .onErrorMap(error -> {
                log.error("[{}] Failed to fetch subjects for schoolId: {}",
                        requestId, schoolCode, error);
                return  new SchoolClassFailureException("Failed to fetch subjects", error);
            });

    }
}
