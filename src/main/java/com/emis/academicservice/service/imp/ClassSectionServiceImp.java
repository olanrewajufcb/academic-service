package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.ClassSectionMapper;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.service.ClassSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClassSectionServiceImp implements ClassSectionService {

    private final ClassSectionRepository repository;
    private final ClassSectionMapper mapper;
    private final TransactionalOperator transactionalOperator;
    @Override
    public Mono<ClassSectionResponse> createClassSection(CreateClassSectionRequest request, String requestId) {
        ClassSection section = mapper.toEntity(request);
    return repository.save(section)
            .as(transactionalOperator::transactional)
            .doOnSuccess(savedClass -> log.info("Successfully created class section with id {} | requestId: {}",
                    savedClass.getSectionId(), requestId))
            .map(mapper::toResponse)
            .onErrorMap(ex -> {
                if (ex instanceof DuplicateKeyException) {
                    return new AlreadyExistsException("Class section already exists");
                } else if (ex instanceof DataIntegrityViolationException) {
                    return new ClassSectionCreationException("Failed to create class section", ex);
                }
                        log.error("Failed to create class section | requestId {} | error {}", requestId, ex.getMessage());
                        return new ClassSectionCreationException("Failed to create class section " + requestId, ex);
                    });
    }

    @Override
    public Mono<Page<ClassSectionResponse>> getAllClassSectionsByClassId(Long classId,
                                           Pageable pageable, String requestId) {
        if (classId == null || classId <= 0) {
            return Mono.error(new InvalidParameterException("classId must be positive"));
        }
        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();
        String sortBy = pageable.getSort().stream().findFirst()
                .map(Sort.Order::getProperty)
                .orElse("sectionId");

    return Mono.zip(
            repository.findPageByClassId(classId, sortBy, pageSize, offset).collectList(),
            repository.countByClassId(classId))
        .timeout(Duration.ofSeconds(5))
        .flatMap(
            tuple -> {
              List<ClassSection> sections = tuple.getT1();
              long total = tuple.getT2();

              List<ClassSectionResponse> responses =
                  total == 0 ? List.of() : sections.stream().map(mapper::toResponse).toList();
              Page<ClassSectionResponse> responsePage = new PageImpl<>(responses, pageable, total);
              return Mono.just(responsePage);
            })
        .doOnSuccess(
            page ->
                log.info(
                    "[{}] Retrieved {} class sections (page {}/{}) for classId: {}",
                    requestId,
                    page.getNumberOfElements(),
                    page.getNumber() + 1,
                    page.getTotalPages(),
                    classId))
        .onErrorMap(
            error -> {
              log.error(
                  "[{}] Failed to fetch class sections for classId: {}", requestId, classId, error);
              if (error instanceof TimeoutException) {
                return new DatabaseTimeoutException(
                    "Timeout while fetching class sections for classId:::: ", error);
              }
              return new ClassSectionFailureException(
                  "Failed to fetch class sections for classId: " + classId, error);
            });
    }
}
