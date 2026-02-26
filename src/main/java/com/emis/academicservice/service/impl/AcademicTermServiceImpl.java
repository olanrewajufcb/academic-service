package com.emis.academicservice.service.impl;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.AcademicTerm;
import com.emis.academicservice.dto.request.CreateAcademicTermRequest;
import com.emis.academicservice.dto.response.AcademicTermResponse;
import com.emis.academicservice.exception.ResourceNotFoundException;
import com.emis.academicservice.repository.AcademicTermRepository;
import com.emis.academicservice.service.AcademicTermService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class AcademicTermServiceImpl implements AcademicTermService {

    private final AcademicTermRepository academicTermRepository;
    private final SchoolCacheService schoolCacheService;
    @Override
    public Mono<AcademicTermResponse> createAcademicTerm(CreateAcademicTermRequest request, String schoolCode, String requestId) {

    // TODO: to implement idempotency key later
    return schoolCacheService
        .getSchoolIdByCode(schoolCode)
        .flatMap(
            schoolId ->
                academicTermRepository
                    .save(
                        AcademicTerm.builder()
                            .termCode(request.termCode())
                            .academicYear(request.academicYear())
                            .name(request.name())
                            .startDate(request.startDate())
                            .endDate(request.endDate())
                            .schoolId(schoolId)
                            .build())
                    .map(AcademicTermResponse::fromEntity))
        .doOnSuccess(response -> log.info("Created academic term with id: {}", response.termId()))
        .doOnError(
            throwable -> log.error("Error creating academic term: {}", throwable.getMessage()))
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex -> {
              log.error("[{}] Error creating academic term: ", requestId, ex);
              return new ResourceNotFoundException(
                  String.format(
                      "Academic term '%s' already exists for school '%s'",
                      request.termCode(), request.name()));
            });
    }

    @Override
    public Mono<AcademicTermResponse> getAcademicTerm(String schoolCode, Long academicTermId, String requestId) {
        return academicTermRepository.findById(academicTermId)
                .map(AcademicTermResponse::fromEntity);
    }
}
