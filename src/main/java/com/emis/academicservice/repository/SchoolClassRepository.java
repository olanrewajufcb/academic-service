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

    @Query("""
        SELECT *  FROM school_classes  WHERE academic_year = :academicYear
        JOIN enrollments e ON sc.class_id = e.class_id 
        JOIN section_enrollments se ON e.enrollment_id = se.enrollment_id
        WHERE e.student_id = se.student_id AND = :studentId
        GROUP BY section_id
    """)
    Flux<StudentClassesPerYear> getStudentClassesPerAcademicYear(Long studentId, String academicYear);

  @Query(
      """
     SELECT e.student_id AS studentId,
               sc.class_id AS classId,
               sc.class_name AS className,
               sc.class_level AS classLevel
        FROM enrollment e
        JOIN school_classes sc ON sc.class_id = e.class_id
        WHERE e.class_id = :classId
    """)
  Flux<StudentsInClassRow> getAllStudentsClass(Long classId);
}
