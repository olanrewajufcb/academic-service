package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Enrollment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EnrollmentRepository extends R2dbcRepository<Enrollment, Long> {

    Mono<Enrollment> findByStudentIdAndClassId(Long studentId, Long classId);
}
