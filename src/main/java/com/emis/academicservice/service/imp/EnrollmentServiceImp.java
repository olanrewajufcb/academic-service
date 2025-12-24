package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.EnrollmentMapper;
import com.emis.academicservice.repository.EnrollmentRepository;
import com.emis.academicservice.repository.SchoolClassProjection;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.service.EnrollmentService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.yaml.snakeyaml.util.Tuple;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Slf4j
@Service
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentClientService studentClientService;
    private final EnrollmentMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClassRepository schoolClassRepository;

    @Override
    public Mono<EnrollmentResponse> enrollStudent(EnrollStudentRequest request, String idempotencyKey) {
        return transactionalOperator.execute(status ->
                         schoolClassRepository.findClassBySchoolCodeAndId(request.getSchoolCode(), request.getClassId())
                        .switchIfEmpty(Mono.error(new SchoolClassNotFoundException(
                                "Class not found: " + request.getClassId())))
                        .flatMap(classProjection ->
                                studentClientService.getStudentDetails(request.getStudentNumber(), request.getSchoolCode())
                        .flatMap(student ->
                                enrollmentRepository.existsByStudentIdAndClassId(student.studentId(), request.getClassId())
                                        .flatMap(exists ->
                                                Boolean.TRUE.equals(exists)
                                                        ? Mono.error(new StudentAlreadyEnrolledException("Student " +
                                                        student.studentId() + " already enrolled in class " + request.getClassId()))
                                                        : Mono.just(Tuples.of(student, classProjection)))))
                        .flatMap(tuple -> {
                            StudentDetailsResponse student = tuple.getT1();
                            SchoolClassProjection classProjection = tuple.getT2();

                            Enrollment enrollment = mapper.toEntity(request);

                            enrollment.setStudentId(student.studentId());
                            enrollment.setStudentNumber(student.studentNumber());
                            enrollment.setStudentName(student.firstName());
                            enrollment.setAcademicYear(classProjection.getAcademicYear());
                            enrollment.setIdempotencyKey(idempotencyKey);

                            return enrollmentRepository.save(enrollment)
                                    .then(schoolClassRepository.incrementStudentCount(request.getClassId()))
                                    .thenReturn(enrollment);
                        })
        )
                .next()
                .doOnNext(saved ->
                        log.info("Student enrolled: id={}, studentId={}, classId={}, idempotencyKey={}",
                                saved.getEnrollmentId(), saved.getStudentId(), saved.getClassId(), idempotencyKey))
                .map(mapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    if (ex.getMessage().contains("chk_class_capacity")) {
                        return new ClassCapacityExceededException("Class capacity exceeded", ex);
                    } else if (ex.getMessage().contains("enrollment_student_class_key")) {
                        return new StudentAlreadyEnrolledException("Duplicate enrollment");
                    }
                    return new EnrollmentFailureException("DB error", ex);
                })
                .onErrorResume(ex -> {
                    log.error("Enrollment failed [{}]: {}", idempotencyKey, ex.toString());
                    return Mono.error(ex);
                });

    }
}