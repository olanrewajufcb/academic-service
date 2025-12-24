package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.AcademicTerm;
import com.emis.academicservice.dto.response.StudentMarksResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcademicTermRepository extends R2dbcRepository<AcademicTerm, Long> {

  @Query(
"""
SELECT 
       at.term_id, 
       at.start_date,
       at.end_date,
       cs.school_id,
       cs.academic_term,
       cs.subject_id,
       ts.student_id,
       ts.section_id,
       ts.term_id,
       ts.total_score,
       ts.average_score,
       ts.position_in_class,
       ts.remarks
FROM academic_term AS at
JOIN term_scores AS ts ON at.term_id = ts.term_id
JOIN class_sections AS cs ON cs.section_id = ts.section_id                      
WHERE ts.student_id = $1 
    AND 
    cs.school_id = $2
AND 
    at.academic_year = $3
   ORDER BY  at.start_date DESC, cs.subject_id
        LIMIT $4 OFFSET $5
        """)
  Flux<StudentMarksResponse> getStudentMarks(
      Long studentId, Long schoolId, String academicYear, int size, long offset);


    @Query("""
     SELECT COUNT(DISTINCT ts.term_scores_id) FROM 
    term_scores ts
    JOIN academic_term at ON at.term_id = ts.term_id
    JOIN class_sections cs ON cs.section_id = ts.section_id
    WHERE sc.academic_year = $1
    AND ts.student_id = $2
""")
    Mono<Long> countStudentMarks(String academicYear, Long studentId);


  @Query("""
        SELECT DISTINCT cs.subject_id 
        FROM term_scores ts
        JOIN class_sections cs ON cs.section_id = ts.section_id
        JOIN academic_term at ON at.term_id = ts.term_id
        WHERE ts.student_id = $1 AND at.academic_year = $2
    """)
  Flux<Long> getStudentSubjectIds(Long studentId, String academicYear);

}






