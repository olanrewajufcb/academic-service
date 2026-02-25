package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.SectionEnrollment;
import com.emis.academicservice.dto.response.SubjectsProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface SectionEnrollmentRepository extends R2dbcRepository<SectionEnrollment, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM section_enrollments WHERE student_id = $1 AND section_id = $2 AND is_deleted = FALSE)")
    Mono<Boolean> existsByStudentIdAndSectionId(Long studentId, Long sectionId);


    @Query("SELECT EXISTS(SELECT 1 FROM section_enrollments WHERE student_number = $1 AND section_id = $2 AND is_deleted = FALSE)")
    Mono<Boolean> existsByStudentNumberAndSectionId(String studentNumber, Long sectionId);

  @Query(
"""
            SELECT se.section_id,
                   se.student_id,
                   se.enrollment_date,
                   cs.class_id,
                   cs.staff_code,
                   cs.teacher_name,
                   s.subject_code,
                   s.name,
                   s.grade_level,
                   s.stage,
                   s.status
                   FROM section_enrollments se
                   JOIN class_sections cs ON cs.section_id = se.section_id
                   JOIN subjects s ON s.subject_id = cs.subject_id
                    WHERE cs.school_code = $1
                    AND se.student_number = $2
                    AND se.is_deleted = FALSE
                 ORDER BY class_id
              LIMIT $3 OFFSET $4
""")
  Flux<SubjectsProjection> findAllClassSectionsBySchoolAndStudent(
      String schoolCode, String studentNumber,
      int size, long offset);

  @Query(
"""
           SELECT COUNT(*)
            FROM section_enrollments se
            JOIN class_sections cs ON cs.section_id = se.section_id
            WHERE cs.school_code = $1
            AND se.student_number = $2
            AND se.is_deleted = FALSE
""")
  Mono<Long> countAllClassSectionsBySchoolAndStudent(String schoolCode, String studentNumber);


  @Query("""
    SELECT *
    FROM section_enrollments
    WHERE student_number = $1 AND section_id = $2 AND is_deleted = FALSE
""")
    Mono<SectionEnrollment> findByStudentNumberAndSectionId(String studentNumber, Long sectionId);



    @Query("""
    UPDATE section_enrollments
    SET is_deleted = TRUE, deleted_at = NOW()
    WHERE student_id = $1 AND section_id = $2 AND is_deleted = FALSE
""")
    Mono<Integer> softDeleteByStudentNumberAndSectionId(Long studentId, Long sectionId);
}
