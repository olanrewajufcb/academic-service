package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.enums.AssessmentType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssessmentRepository extends R2dbcRepository<Assessment, Long> {


    @Query("""
        SELECT a.*, at.name FROM assessments a
        JOIN class_sections cs ON a.section_id = cs.section_id
        JOIN school_classes sc ON cs.class_id = sc.class_id
        JOIN academic_term at ON a.term_id = at.term_id
        WHERE a.section_id = :sectionId
        AND sc.school_id = :schoolId
        AND (:assessmentType IS NULL OR a.assessment_type = :assessmentType)
        AND (:term IS NULL OR at.term_code = :term)
        ORDER BY a.due_date DESC
        LIMIT :size OFFSET :offset AND a.is_deleted = FALSE
    """)
    Flux<Assessment> findBySectionIdAndSchoolId(
            @Param("sectionId") Long sectionId,
            @Param("schoolId") Long schoolId,
            @Param("assessmentType") String assessmentType,
            @Param("term") String term,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query("""
        SELECT COUNT(*) FROM assessments a
        JOIN class_sections cs ON a.section_id = cs.section_id  
        JOIN school_classes sc ON cs.class_id = sc.class_id
        JOIN academic_term at ON a.term_id = at.term_id
        WHERE a.section_id = :sectionId
        AND sc.school_id = :schoolId
        AND (:assessmentType IS NULL OR a.assessment_type = :assessmentType)
        AND (:term IS NULL OR at.term_code = :term AND a.is_deleted = FALSE)
    """)
    Mono<Long> countBySectionIdAndSchoolId(
            @Param("sectionId") Long sectionId,
            @Param("schoolId") Long schoolId,
            @Param("assessmentType") String assessmentType,
            @Param("term") String term);


    @Query("""
        SELECT a.* FROM assessment a
        JOIN class_sections cs ON a.section_id = cs.section_id
        JOIN school_classes sc ON cs.class_id = sc.class_id
        WHERE a.assessment_id = :assessmentId AND sc.school_id = :schoolId AND a.is_deleted = FALSE
    """)
    Mono<Assessment> findByAssessmentIdAndSchoolId(Long assessmentId, Long schoolId);

}
