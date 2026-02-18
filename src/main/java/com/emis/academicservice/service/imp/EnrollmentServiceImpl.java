package com.emis.academicservice.service.imp;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.dto.response.StudentEnrollmentResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.EnrollmentMapper;
import com.emis.academicservice.repository.EnrollmentRepository;
import com.emis.academicservice.repository.SchoolClassProjection;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.service.ActiveStudentEnrollmentCache;
import com.emis.academicservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Slf4j
@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClassRepository schoolClassRepository;
    private final ActiveStudentEnrollmentCache studentEnrollmentCache;

    @Override
    public Mono<EnrollmentResponse> placeStudentInClass(EnrollStudentRequest request, String idempotencyKey) {
    return schoolClassRepository
        .findClassBySchoolCodeAndId(request.getSchoolCode(), request.getClassId())
        .switchIfEmpty(
            Mono.error(
                new SchoolClassNotFoundException("Class not found: " + request.getClassId())))
        .flatMap(
            classProjection ->
                studentEnrollmentCache
                    .getStudentEnrollmentFromCache(request.getSchoolCode(), request.getStudentNumber(),
                            classProjection.getAcademicYear())
                    .flatMap(
                        student -> {
                            log.info(
                                "Student found: id={}, name={}, number={}",
                                student.studentId(),
                                student.studentName(),
                                student.studentNumber());
                          return  enrollmentRepository
                                    .existsByStudentIdAndClassId(
                                            student.studentId(), request.getClassId())
                                    .flatMap(
                                            exists ->
                                                    Boolean.TRUE.equals(exists)
                                                            ? Mono.error(
                                                            new AlreadyExistsException(
                                                                    "Student "
                                                                            + student.studentNumber()
                                                                            + " already enrolled in class "
                                                                            + request.getClassId()))
                                                            : Mono.just(Tuples.of(student, classProjection)));

                        }))

        .flatMap(
            tuple -> {
              StudentEnrollmentResponse student = tuple.getT1();
              SchoolClassProjection  classProjection = tuple.getT2();

                Enrollment enrollment = mapper.toEntity(request);

              enrollment.setStudentId(student.studentId());
              enrollment.setStudentNumber(student.studentNumber());
              enrollment.setStudentName(student.studentName());
              enrollment.setIdempotencyKey(idempotencyKey);

              // Perform the save and increment operations within a transaction
              return enrollmentRepository
                  .save(enrollment)
                  .flatMap(
                      savedEnrollment ->
                          schoolClassRepository
                              .incrementStudentCount(request.getClassId())
                              .thenReturn(Tuples.of(savedEnrollment, classProjection.getClassName())))
                  .as(transactionalOperator::transactional);
            })
        .doOnNext(
            saved ->
                log.info(
                    "Student enrolled: id={}, studentId={}, enrollmentDate={}, status={}, idempotencyKey={}",
                    saved.getT1().getEnrollmentId(),
                    saved.getT1().getStudentId(),
                    saved.getT1().getEnrollmentDate(),
                    saved.getT1().getEnrollmentStatus(),
                    idempotencyKey))
        .map(savedEntity ->
                EnrollmentResponse.from(savedEntity.getT1(), request.getSchoolCode(),  savedEntity.getT2()))
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex -> {
              if (ex.getMessage().contains("chk_class_capacity")) {
                return new ClassCapacityExceededException("Class capacity exceeded", ex);
              } else if (ex.getMessage().contains("enrollment_student_class_key")) {
                return new AlreadyExistsException("Duplicate enrollment");
              }
              return new EnrollmentFailureException("DB error::::", ex);
            });
    }

    @Override
    public Mono<EnrollmentResponse> getStudentPlacement(Long classId,
                                    String studentNumber, String schoolCode, String requestId) {
        return schoolClassRepository
                .findClassBySchoolCodeAndId(schoolCode, classId)
                .switchIfEmpty(
                        Mono.error(
                                new SchoolClassNotFoundException("Class not found: " + classId)))
                .flatMap(
                        classProjection -> enrollmentRepository
                                .findByStudentNumberAndClassId(studentNumber, classProjection.getClassId())
                        .switchIfEmpty(
                                Mono.error(
                                        new StudentNotFoundException("Student not found: " + studentNumber)))
                        .map(enrollment ->
                                EnrollmentResponse.from(enrollment, schoolCode, classProjection.getClassName()))
                );
    }
}