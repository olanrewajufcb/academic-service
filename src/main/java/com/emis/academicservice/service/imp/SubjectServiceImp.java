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

            .onErrorMap(ex -> {

                if (ex instanceof DataIntegrityViolationException){
                    if (ex.getMessage() != null &&
                            ex.getMessage().contains("uk_school_subject_code")) {
                        return new AlreadyExistsException(
                                String.format("Subject '%s' already exists for school '%s'",
                                        request.getSubjectCode(), request.getSchoolCode()));
                    }
                }
                return new AcademicServiceFailureException("Database error", ex.getMessage(),  ex);
            });
    }

    public Mono<Page<SubjectResponse>> getSubjectBySchoolAndClassLevel(String schoolCode, String gradeLevel, Pageable pageable, String requestId) {

        return schoolCacheService
                .getSchoolIdByCode(schoolCode)
                .switchIfEmpty(Mono.error(new SchoolNotFoundException("School not found for " + schoolCode)))
                .flatMap(schoolId -> Mono.zip(
                                subjectRepository.findBySchoolIdAndGradeLevel(
                                        schoolId, gradeLevel, pageable.getPageSize(), pageable.getOffset()).collectList(),
                                subjectRepository.countBySchoolIdAndClassLevel(schoolId, gradeLevel))
                        .timeout(Duration.ofSeconds(3))
                        .map(tuple -> {
                            List<Subject> subjects = tuple.getT1();
                            long total = tuple.getT2();

                            List<SubjectResponse> responses = total == 0
                                    ? List.of()
                                    : subjects.stream().map(subjectMapper::toResponse).toList();

                            return (Page<SubjectResponse>) new PageImpl<>(responses, pageable, total);
                        }))
                .doOnError(error -> log.error("[{}] Error fetching subjects for school {}: {}",
                        requestId, schoolCode, error.getMessage()))

                .onErrorMap(error -> {
                    if (error instanceof TimeoutException){
                        return new DatabaseTimeoutException("Database operation timed out", error);
                    }
                    return new AcademicServiceFailureException(
                            "Failed to fetch subjects :::" + error.getMessage());
                });
    }
}
