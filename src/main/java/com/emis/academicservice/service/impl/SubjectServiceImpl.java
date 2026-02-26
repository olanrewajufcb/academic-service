package com.emis.academicservice.service.impl;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.SubjectMapper;
import com.emis.academicservice.repository.SubjectRepository;
import com.emis.academicservice.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final SchoolCacheService schoolCacheService;
    private final TransactionalOperator transactionalOperator;
    private final ServiceConfigurationProperties properties;



    @Override
    public Mono<SubjectResponse> registerSubject(RegisterSubjectRequest request, String requestId) {
    return schoolCacheService
        .getSchoolIdByCode(request.getSchoolCode())
        .flatMap(
            schoolId -> {
              Subject subject = subjectMapper.toEntity(request);
              subject.setSchoolId(schoolId);

              log.info("[{}] Registering subject::::: {}", requestId, subject);
              return subjectRepository
                  .save(subject)
                  .as(transactionalOperator::transactional)
                  .map(subjectMapper::toResponse)
                  .onErrorMap(
                      DataIntegrityViolationException.class,
                      ex -> {
                        log.error("[{}] Error registering subject: ", requestId, ex);
                        if (ex.getMessage().contains("violates not-null constraint")) {
                          return new ValidationException("Required parameter missing ::: ");
                        }
                        return new ResourceNotFoundException(
                            String.format(
                                "Subject '%s' already exists for school '%s'",
                                request.getSubjectCode(), request.getSchoolCode()));
                      });
            });
    }


    public Mono<Page<SubjectResponse>> getSubjectBySchoolAndClassLevel(String schoolCode, String gradeLevel, Pageable pageable, String requestId) {

    return Mono.zip(
            subjectRepository
                .findBySchoolCodeAndGradeLevel(
                    schoolCode, gradeLevel, pageable.getPageSize(), pageable.getOffset())
                .collectList(),
            subjectRepository.countBySchoolCodeAndGradeLevel(schoolCode, gradeLevel))
        .timeout(Duration.ofSeconds(properties.getTimeout()))
        .map(
            tuple -> {
              List<Subject> subjects = tuple.getT1();
              long total = tuple.getT2();

              List<SubjectResponse> responses =
                  total == 0
                      ? List.of()
                      : subjects.stream().map(subjectMapper::toResponse).toList();

              return (Page<SubjectResponse>) new PageImpl<>(responses, pageable, total);
            })
        .doOnError(
            error ->
                log.error(
                    "[{}] Error fetching subjects for school {}: {}",
                    requestId,
                    schoolCode,
                    error.getMessage()))
        .onErrorMap(
            error -> {
              if (error instanceof TimeoutException) {
                return new DatabaseTimeoutException("Database operation timed out", error);
              }
              return new AcademicServiceFailureException(
                  "Failed to fetch subjects :::" + error.getMessage());
            });
    }

    @Override
    public Mono<Page<SubjectResponse>> getAllSubjectsBySchoolCode(String schoolCode,
                                         Pageable pageable, String requestId) {
        return  Mono.zip(subjectRepository.findBySchoolCode(
                                schoolCode, pageable.getPageSize(), pageable.getOffset()).collectList(),
                        subjectRepository.countBySchoolCode(schoolCode))
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .map(tuple -> {
                    List<Subject> subjects = tuple.getT1();
                    long total = tuple.getT2();

                    List<SubjectResponse> responses = total == 0
                            ? List.of()
                            : subjects.stream().map(subjectMapper::toResponse).toList();

                    return (Page<SubjectResponse>) new PageImpl<>(responses, pageable, total);
                })
                .doOnError(error -> log.error("[{}] Error fetching subjects for school {}: {}",
                        requestId, schoolCode, error.getMessage()))

                .onErrorMap(error -> {
                    if (error instanceof TimeoutException){
                        return new DatabaseTimeoutException("Database operation timed out", error);
                    }
                    return new AcademicServiceFailureException(
                            "Failed to fetch subjects :::" + error.getMessage());
                });    }
}
