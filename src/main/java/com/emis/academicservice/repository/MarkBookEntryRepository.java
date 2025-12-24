package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.MarkBookEntry;
import com.emis.academicservice.dto.response.MarkBookEntryDetail;
import com.emis.academicservice.dto.response.MarkBookViewResponse;
import com.emis.academicservice.enums.AssessmentType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MarkBookEntryRepository extends R2dbcRepository<MarkBookEntry, Long> {
    @Query("SELECT EXISTS(SELECT 1 FROM markbook_entry WHERE assessment_id = :assessmentId AND student_id = :studentId)")
    Mono<Boolean> existsByAssessmentIdAndStudentId(Long assessmentId, Long studentId);

    @Query("SELECT * FROM markbook_entry WHERE assessment_id = :assessmentId ORDER BY student_id")
    Flux<MarkBookEntry> findByAssessmentId(Long assessmentId);

    @Query("""
        SELECT COUNT(*) FROM markbook_entry me
        JOIN assessment a ON me.assessment_id = a.assessment_id
        WHERE a.section_id = :sectionId AND a.assessment_type = :assessmentType
    """)
    Mono<Long> countBySectionAndType(Long sectionId, AssessmentType assessmentType);

  @Query("""
 SELECT 
        m.mark_entry_id,
        m.assessment_id,
        m.student_id,
        m.score_obtained,
        m.score_percentage,
        m.remark,
        m.marked_at,
        a.max_score,
        a.name as assessment_name,
        cs.subject_id,
        cs.academic_term,
        s.subject_name
       
 FROM markbook_entry m  
 JOIN assessments a ON a.assessment_id = m.assessment_id
 JOIN class_sections cs ON cs.section_id = a.section_id
 JOIN subjects s ON s.subject_id = cs.subject_id
 WHERE cs.section_id = $1
 AND 
     m.assessment_id = $2
 AND cs.academic_term = $3
 ORDER BY m.student_id
 
""")
  Flux<MarkBookEntryDetail> findBySectionAndAssessmentAndAcademicYear(Long sectionId, Long assessmentId, String academicYear);
}

