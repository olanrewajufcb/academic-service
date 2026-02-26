package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.SchoolClass;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SchoolClassRepository extends R2dbcRepository<SchoolClass, Long> {


  @Query(
      """
            SELECT class_id, school_id, class_name, grade_level, arm, academic_year,
            form_teacher_id, max_students, current_students, created_at, updated_at
            FROM school_classes WHERE school_code = :schoolCode AND academic_year = :academicYear
                                AND is_deleted = FALSE
            ORDER BY class_id LIMIT :size OFFSET :offset
            
            """)
  Flux<SchoolClass> findBySchoolCodeAndAcademicYear(
      String schoolCode, String academicYear, int size, long offset);

    @Query("SELECT COUNT(*) FROM school_classes WHERE school_code = $1 AND academic_year = $2 AND is_deleted = FALSE")
    Mono<Long> countBySchoolCodeAndAcademicYear(String schoolCode, String academicYear);

  @Query("""
        SELECT 
            sc.class_id, 
            sc.class_name, 
            sc.grade_level, 
            sc.arm,
            STRING_AGG(s.name, ', ') AS subjects
        FROM school_classes AS sc
        JOIN class_sections As cs ON sc.class_id = cs.class_id
        JOIN section_enrollments se ON cs.section_id = se.section_id
        JOIN subjects s ON cs.subject_id = s.subject_id
        WHERE sc.academic_year  = $1
        AND se.student_id = $2 AND sc.is_deleted = FALSE
        GROUP BY sc.class_id, sc.class_name, sc.grade_level, sc.arm
        ORDER BY sc.class_name
        LIMIT $3 OFFSET $4
    """)
  Flux<StudentClassesPerYear> getStudentClassesPerAcademicYear(String academicYear,Long studentId,
                                                               int limit, long offset);

    @Query("""
     SELECT COUNT(DISTINCT sc.class_id) FROM 
    school_classes sc
    JOIN class_sections cs ON sc.class_id = cs.class_id
    JOIN section_enrollments se ON cs.section_id = se.class_id
    JOIN subjects s ON cs.subject_id = s.subject_id
    WHERE sc.academic_year = $1
    AND se.student_id = $2 AND sc.is_deleted = FALSE
""")
    Mono<Long> countStudentClasses(String academicYear, Long studentId);

  @Query(
      """
     SELECT e.student_id AS student_id,
            e.student_number AS student_number,
            e.student_name AS student_name,
               sc.school_id AS school_id,
               sc.school_code AS school_code,
               sc.school_name AS school_name,
               sc.class_id AS class_id,
               sc.form_teacher_id AS form_teacher_id,
               sc.class_name AS class_name,
               sc.grade_level AS grade_level,
               sc.arm AS arm,
               sc.academic_year AS academic_year
        FROM enrollments e
        JOIN school_classes sc ON sc.class_id = e.class_id
        WHERE e.class_id = $1 AND e.enrollment_status = 'ENROLLED' AND e.is_deleted = FALSE
        ORDER BY
            CASE
                        WHEN $4 = 'student_name' THEN e.student_name
                        WHEN $4 = 'student_number' THEN e.student_number
                        ELSE e.student_name
                    END
        LIMIT $2 OFFSET $3
    """)
  Flux<StudentsInClassRow> getAllStudentsInClass(Long classId, int limit, long offset, String sortColumn);

    @Query("SELECT COUNT(*) FROM enrollments WHERE class_id = $1 AND enrollment_status = 'ENROLLED' AND is_deleted = FALSE")
    Mono<Long> countActiveStudentsInClass(Long classId);

  @Query(
      "UPDATE school_classes SET current_students = current_students + 1, updated_at = CURRENT_TIMESTAMP WHERE class_id = :classId")
  Mono<Integer> incrementStudentCount(Long classId);



    @Query("""
    SELECT 
        sc.class_id, 
        sc.school_id, 
        sc.school_code, 
        sc.academic_year,
        sc.class_name, 
        sc.grade_level
    FROM school_classes sc
    WHERE sc.school_code = $1 
      AND sc.class_id = $2 
      AND sc.is_deleted = FALSE
    """)
    Mono<SchoolClassProjection> findClassBySchoolCodeAndId(
            String schoolCode, Long classId);
}
