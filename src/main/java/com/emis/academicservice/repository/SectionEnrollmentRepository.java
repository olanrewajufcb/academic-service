package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.SectionEnrollment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;


public interface SectionEnrollmentRepository extends R2dbcRepository<SectionEnrollment, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM section_enrollments WHERE student_id = $1 AND section_id = $2)")
    Mono<Boolean> existsByStudentIdAndSectionId(Long studentId, Long sectionId);

}
