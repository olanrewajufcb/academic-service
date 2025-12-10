package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.exception.ClassSectionAlreadyExistsException;
import com.emis.academicservice.exception.ClassSectionNotFoundException;
import com.emis.academicservice.exception.SchoolClassCreationException;
import com.emis.academicservice.mapper.ClassSectionMapper;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.service.ClassSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    return repository.findByClassIdAndSubjectId(request.getClassId(), request.getSubjectId())
            .flatMap(duplicate -> Mono.error(
                    new ClassSectionAlreadyExistsException("Class section already exists")))
            .then(Mono.defer(() -> repository.save(section))
                    .as(transactionalOperator::transactional)
                    .doOnSuccess(savedClass -> log.info("Successfully created class section with id {} | requestId: {}",
                            savedClass.getSectionId(), requestId))
                    .map(mapper::toResponse))
                    .onErrorMap(DataIntegrityViolationException.class, ex ->
                            new DataIntegrityViolationException("Data integrity violation: " + ex.getMessage(), ex))
                    .onErrorResume(ex -> {
                        log.error("Failed to create class section | requestId {} | error {}", requestId, ex.getMessage());
                        return Mono.error(new SchoolClassCreationException("Failed to create class section " + requestId, ex));
                    });
    }

    @Override
    public Flux<ClassSectionResponse> getAllClassSectionsByClassId(Long classId, Pageable pageable, String requestId) {
    return repository.findByClassId(classId, pageable.getPageSize(), pageable.getOffset())
            .switchIfEmpty(Mono.error(new ClassSectionNotFoundException(
                    String.format("No class section found for classId %d", classId))))
            .map(mapper::toResponse);
    }
}
