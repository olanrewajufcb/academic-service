package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Enrollment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EnrollmentRepository extends R2dbcRepository<Enrollment, Long> {

    Mono<Enrollment> findByStudentIdAndClassId(Long studentId, Long classId);

    @Query(""" 
            SELECT EXISTS(SELECT 1 FROM enrollments WHERE student_id = $1
            AND class_id = $2 AND is_deleted = FALSE)
""")
    Mono<Boolean> existsByStudentIdAndClassId(Long studentId, Long classId);

    Mono<Boolean> existsByStudentNumberAndClassId(String studentNumber, Long classId);

    @Query(""" 
            SELECT * FROM enrollments WHERE student_number = $1
            AND class_id = $2 AND is_deleted = FALSE
""")
    Mono<Enrollment> findByStudentNumberAndClassId(String studentNumber, Long classId);

    // For soft deleting
    @Query("""
    UPDATE enrollments
    SET is_deleted = TRUE, deleted_at = NOW()
    WHERE student_number = $1 AND class_id = $2 AND is_deleted = FALSE
""")
    Mono<Integer> softDeleteByStudentNumberAndClassId(String studentNumber, Long classId);
}
