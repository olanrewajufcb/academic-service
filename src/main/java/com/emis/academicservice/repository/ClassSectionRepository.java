package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.domain.db.SchoolClass;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassSectionRepository extends R2dbcRepository<ClassSection, Long> {
    @Query("""
            SELECT * FROM class_sections WHERE class_id = :classId
            ORDER BY section_id LIMIT :size OFFSET :offset
""" )
    Flux<ClassSection> findByClassId(Long classId, int size, long offset);
    
    @Query("SELECT * FROM class_sections WHERE teacher_id = :teacherId")
    Flux<ClassSection> findByTeacherId(Long teacherId);
    
    @Query("SELECT * FROM class_sections WHERE subject_id = :subjectId")
    Flux<ClassSection> findBySubjectId(Long subjectId);

    Mono<ClassSection> findByClassIdAndSubjectId(Long classId, Long subjectId);


    @Query("""
        SELECT * from class_sections 
        WHERE class_id = $1
        ORDER BY 
            CASE 
                WHEN $2 = 'sectionId' THEN section_id
                WHEN $2 = 'subjectId' THEN subject_id
                ELSE section_id
            END 
        LIMIT  $3 OFFSET $4
    """)
    Flux<ClassSection> findPageByClassId(Long classId, String sortBy, int limit, long offset);

    @Query("SELECT COUNT(*) FROM class_sections WHERE class_id = $1")
    Mono<Long> countByClassId(Long classId);

    Mono<SchoolClass> findBySchoolIdAndClassId(Long schoolId, Long classId);


    @Query("""
    SELECT 
        cs.section_id,
        cs.class_id,
        sc.school_id,
        sc.school_code
    FROM class_sections cs
    JOIN school_classes sc ON cs.class_id = sc.class_id
    WHERE cs.section_id = $1 
      AND sc.school_code = $2
    """)
    Mono<SectionValidationProjection> validateSectionOwnership(Long sectionId, String schoolCode);

    @Query("""
        SELECT cs.* FROM class_sections cs
        JOIN school_classes sc ON cs.class_id = sc.class_id
        JOIN schools s ON sc.school_id = s.school_id
        WHERE cs.section_id = :sectionId 
        AND s.school_code = :schoolCode
    """)
    Mono<ClassSection> findBySectionIdAndSchoolCode(
            @Param("sectionId") Long sectionId,
            @Param("schoolCode") String schoolCode);

    @Query("""
        SELECT cs.* FROM class_sections cs
        JOIN school_classes sc ON cs.class_id = sc.class_id
        WHERE cs.section_id = :sectionId 
        AND sc.school_id = :schoolId
    """)
    Mono<ClassSection> findBySectionIdAndSchoolId(
            @Param("sectionId") Long sectionId,
            @Param("schoolId") Long schoolId);


}

