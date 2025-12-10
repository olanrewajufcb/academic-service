package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.exception.DuplicateEnrollmentException;
import com.emis.academicservice.exception.EnrollmentFailureException;
import com.emis.academicservice.exception.SchoolClassCreationException;
import com.emis.academicservice.exception.SchoolClassNotFoundException;
import com.emis.academicservice.mapper.EnrollmentMapper;
import com.emis.academicservice.repository.EnrollmentRepository;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.service.EnrollmentService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Slf4j
@Service
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepository repository;
    private final StudentClientService studentClientService;
    private final EnrollmentMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClassRepository schoolClassRepository;

    @Override
    public Mono<EnrollmentResponse> enrollStudent(EnrollStudentRequest request, String requestId) {
        return schoolClassRepository.existsById(request.getClassId())
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new SchoolClassNotFoundException("Class not found for the given " + request.getClassId()))
                )
                .then(studentClientService.getStudentDetails(request.getStudentNumber()))
                .flatMap(student -> {

                    Enrollment enrollment = mapper.toEntity(request);
                    enrollment.setStudentId(student.studentId());

                    return repository.findByStudentIdAndClassId(student.studentId(), request.getClassId())
                .flatMap(existing -> Mono.error(
                        new DuplicateEnrollmentException(String.format
                                ("Enrollment with studentId: %s and classId %s already exists",
                                        existing.getStudentId(), existing.getClassId())
                        ))
                )
                .switchIfEmpty(repository.save(enrollment))
                            .cast(Enrollment.class);
                 })
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedClass -> log.info("Student successfully enrolled with id {} | requestId: {}",
                        savedClass.getClassId(), requestId))
                .map(mapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex ->
                        new DataIntegrityViolationException("Data integrity violation: " + ex.getMessage(), ex))
                .onErrorResume(ex -> {
                    log.error("Failed to enroll student to class | requestId {} | error {}", requestId, ex.getMessage());
                    return Mono.error(new EnrollmentFailureException("Failed to enroll student to class " + requestId, ex));
                });

    }


}