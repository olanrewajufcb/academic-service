package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.StudentAttendance;
import com.emis.academicservice.dto.response.StudentAttendanceSummaryProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface StudentAttendanceRepository extends ReactiveCrudRepository<StudentAttendance, Long> {

    @Query("""
SELECT
    COUNT(*) AS total_lessons,
    SUM(CASE WHEN sa.attendance_status = 'PRESENT' THEN 1 ELSE 0 END) AS present,
    SUM(CASE WHEN sa.attendance_status = 'ABSENT' THEN 1 ELSE 0 END) AS absent,
    SUM(CASE WHEN sa.attendance_status = 'LATE' THEN 1 ELSE 0 END) AS late
FROM academic_schema.student_attendance sa
JOIN academic_schema.lessons l
    ON l.lesson_id = sa.lesson_id
WHERE sa.student_number = $1
  AND l.term_id = $2
  AND l.school_code = $3
  AND sa.deleted_at IS NULL
  AND l.deleted_at IS NULL
""")
    Mono<StudentAttendanceSummaryProjection> getAttendanceSummary(
            String studentNumber,
            Long termId,
            String schoolCode
    );


  @Query(
"""
SELECT
    COUNT(l.lesson_id) AS total_lessons,
    COUNT(DISTINCT sa.lesson_id) AS lessons_with_attendance,
    COUNT(l.lesson_id) - COUNT(DISTINCT a.lesson_id) AS lessons_without_attendance
FROM academic_schema.lessons l
LEFT JOIN academic_schema.student_attendance sa
    ON sa.lesson_id = l.lesson_id
    AND sa.deleted_at IS NULL
WHERE l.section_id = $1
  AND l.term_id = $2
  AND l.school_code = $3
  AND l.deleted_at IS NULL;
""")
  Mono<SectionAttendanceAggregateProjection> getSectionAggregate(
      Long sectionId, Long termId, String schoolCode);

    @Query(""" 
SELECT
    sa.student_number,
    COUNT(*) AS total_lessons,
    SUM(CASE WHEN sa.attendance_status = 'PRESENT' THEN 1 ELSE 0 END) AS present
FROM academic_schema.student_attendance sa
JOIN academic_schema.lessons l
    ON l.lesson_id = sa.lesson_id
WHERE l.section_id = $1
  AND l.term_id = $2
  AND l.school_code = $3
  AND sa.deleted_at IS NULL
  AND l.deleted_at IS NULL
GROUP BY sa.student_number
ORDER BY present ASC

""")
    Flux<SectionStudentAttendanceProjection> getStudentBreakdown(
            Long sectionId,
            Long termId,
            String schoolCode
    );


    @Query("""

SELECT
    COUNT(sa.attendance_id) AS total_records,
    SUM(CASE WHEN sa.attendance_status = 'PRESENT' THEN 1 ELSE 0 END) AS present
FROM academic_schema.student_attendance sa
JOIN academic_schema.lessons l
    ON l.lesson_id = sa.lesson_id
WHERE l.section_id = $1
  AND l.term_id = $2
  AND l.school_code = $3
  AND sa.deleted_at IS NULL
  AND l.deleted_at IS NULL;
"""
    )
    Mono<SectionAttendanceStatsProjection> getSectionAttendanceStats(
            Long sectionId,
            Long termId,
            String schoolCode
    );

    @Query("""
SELECT *
        FROM academic_schema.student_attendance 
        WHERE lesson_id = $1 
        AND student_id = $2 
        AND attendance_status = 'PRESENT'
        AND deleted_at = NULL
   """ )
    Mono<StudentAttendance> findByLessonIdAndStudentIdAndDeletedAtIsNull(Long lessonId, Long studentId);
}