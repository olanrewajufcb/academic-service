package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.SchoolClass;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SchoolClassRepository extends R2dbcRepository<SchoolClass, Long> {
    @Query(""" 
            SELECT EXISTS(SELECT 1 FROM school_classes WHERE school_id = :schoolId
            AND class_name = :className AND academic_year = :academicYear)

""")
    Mono<Boolean> existsBySchoolIdAndClassNameAndAcademicYear(
            Long schoolId,
            String className,
            String academicYear
    );

  @Query(
      """
            SELECT class_id, school_id, class_name, class_level, arm, academic_year,
            form_teacher_id, max_students, current_students, created_at, updated_at
            FROM school_classes WHERE school_id = :schoolId AND academic_year = :academicYear
            ORDER BY class_id LIMIT :size OFFSET :offset
            
            """)
  Flux<SchoolClass> findBySchoolIdAndAcademicYear(
      Long schoolId, String academicYear, int size, long offset);

    @Query("SELECT COUNT(*) FROM school_classes WHERE school_id = $1 AND academic_year = $2")
    Mono<Long> countBySchoolIdAndAcademicYear(Long schoolId, String academicYear);

  @Query("""
        SELECT 
            sc.class_id, 
            sc.class_name, 
            sc.class_level, 
            sc.arm,
            STRING_AGG(s.name, ', ') AS subjects
        FROM school_classes AS sc
        JOIN class_sections As cs ON sc.class_id = cs.class_id
        JOIN section_enrollments se ON cs.section_id = se.section_id
        JOIN sujects s ON cs.subject_id = s.suject_id
        WHERE sc.academic_year  = $1
        AND se.student_id = $2
        GROUP BY sc.class_id, sc.class_name, sc.class_level, sc.arm
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
    WHERE sc.academic_year = $1
    AND se.student_id = $2
""")
    Mono<Long> countStudentClasses(String academicYear, Long studentId);

  @Query(
      """
     SELECT e.student_id AS studentId,
            e.student_number AS studentNumber,
            e.student_name AS studentName,
               sc.school_id AS schoolId,
               sc.school_number AS schoolNumber,
               sc.school_name AS schoolName,
               sc.class_id AS classId,
               sc.form_teacher_id AS formTeacherId,
               sc.class_name AS className,
               sc.class_level AS classLevel
               sc.arm AS arm
        FROM enrollments e
        JOIN school_classes sc ON sc.class_id = e.class_id
        WHERE e.class_id = $1 AND enrollment_status = 'ENROLLED'
        ORDER BY
            CASE
                        WHEN $4 = 'student_name' THEN student_name
                        WHEN $4 = 'student_number' THEN student_number
                        ELSE student_name
                    END
        LIMIT $2 OFFSET $3
    """)
  Flux<StudentsInClassRow> getAllStudentsClass(Long classId, int limit, long offset, String sortColumn);

    @Query("SELECT COUNT(*) FROM enrollments WHERE class_id = $1 AND enrollment_status = 'ENROLLED'")
    Mono<Long> countActiveStudentsInClass(Long classId);

  @Query(
      "UPDATE school_classes SET current_students = current_students + 1, updated_at = CURRENT_TIMESTAMP WHERE class_id = :classId")
  Mono<Integer> incrementStudentCount(Long classId);

        @Query("UPDATE school_classes SET current_students = current_students - 1, updated_at = CURRENT_TIMESTAMP WHERE class_id = :classId AND current_students > 0")
        Mono<Integer> decrementStudentCount(Long classId);

        @Query("UPDATE school_classes SET current_students = (SELECT COUNT(*) FROM enrollment WHERE class_id = :classId AND status = 'ENROLLED'), updated_at = CURRENT_TIMESTAMP WHERE class_id = :classId")
        Mono<Integer> recalculateStudentCount(Long classId);

    @Query("""
    SELECT 
        sc.class_id, sc.school_id, sc.school_code, sc.academic_year,
        sc.class_name, sc.class_level
    FROM school_classes sc
    WHERE sc.school_code = $1 
      AND sc.class_id = $2
    """)
    Mono<SchoolClassProjection> findClassBySchoolCodeAndId(
            String schoolCode, Long classId);
}
