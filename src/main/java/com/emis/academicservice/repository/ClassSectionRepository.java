package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.ClassSection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassSectionRepository extends R2dbcRepository<ClassSection, Long> {

    @Query("""
        SELECT * from class_sections 
        WHERE class_id = $1 AND is_deleted = FALSE
        ORDER BY 
            CASE 
                WHEN $2 = 'sectionId' THEN section_id
                WHEN $2 = 'subjectId' THEN subject_id
                ELSE section_id
            END 
        LIMIT $3 OFFSET $4 
    """)
    Flux<ClassSection> findPageByClassId(Long classId, String sortBy, int limit, long offset);

    @Query("SELECT COUNT(*) FROM class_sections WHERE class_id = $1 AND is_deleted = FALSE")
    Mono<Long> countByClassId(Long classId);



    @Query("""
    SELECT 
        cs.section_id,
        cs.class_id,
        sc.school_id,
        sc.school_code,
        sc.academic_year
    FROM class_sections cs
    JOIN school_classes sc ON cs.class_id = sc.class_id
    WHERE cs.section_id = $1 
      AND sc.school_code = $2
    """)
    Mono<SectionValidationProjection> validateSectionOwnership(Long sectionId, String schoolCode);

    @Query("""
        SELECT cs.* FROM class_sections cs
        JOIN school_classes sc ON cs.class_id = sc.class_id
        WHERE cs.section_id = :sectionId 
        AND cs.school_code = :schoolCode AND cs.is_deleted = FALSE
    """)
    Mono<ClassSection> findBySectionIdAndSchoolCode(
            @Param("sectionId") Long sectionId,
            @Param("schoolCode") String schoolCode);

    @Query("""
        SELECT cs.* FROM class_sections cs
        JOIN school_classes sc ON cs.class_id = sc.class_id
        WHERE cs.section_id = :sectionId 
        AND cs.school_id = :schoolId AND cs.is_deleted = FALSE
    """)
    Mono<ClassSection> findBySectionIdAndSchoolId(
            @Param("sectionId") Long sectionId,
            @Param("schoolId") Long schoolId);

    @Query("""
        SELECT cs.* FROM class_sections cs
        WHERE cs.class_id = $1 
        AND cs.staff_code = $2 AND cs.is_deleted = FALSE
    """)
    Mono<ClassSection> findByClassIdAndSectionIdAndTeacherCode(Long classId, String staffCode);

    @Query("""
    UPDATE academic_schema.class_sections
    SET teacher_id = NULL,
        staff_code = NULL,
        teacher_name = ''
    WHERE school_code = :schoolCode
      AND teacher_id = :staffId AND is_deleted = FALSE
""")
    Mono<Integer> unassignStaffFromSchool(String schoolCode, Long staffId);


  @Query(
      """
        SELECT cs.section_id,
               cs.room,
               cs.max_capacity,
               cs.current_enrollment,
               cs.staff_code,
               cs.teacher_name,
               s.school_code,
               s.subject_code,
               s.name as subject_name,
               s.grade_level
               from class_sections cs
                JOIN subjects s ON 
                cs.subject_id = s.subject_id 
        WHERE  cs.class_id = $1 
          AND cs.is_deleted = FALSE
        AND s.is_deleted = FALSE
        ORDER BY
            CASE
                WHEN $2 = 'sectionId' THEN cs.section_id
                WHEN $2 = 'subjectId' THEN cs.subject_id
                ELSE cs.section_id
            END
        LIMIT $3 OFFSET $4
    """)
  Flux<SubjectWithSectionProjection> findPageByClassIdThenJoinWithSubject(
      Long classId, String sortBy, int limit, long offset);

  @Query("""
    SELECT EXISTS(
        SELECT 1 FROM class_sections
        WHERE school_code = $1 AND is_deleted = FALSE
    )
""")
  Mono<Boolean> existsBySchoolCode(String schoolCode);



    @Query(
            """
              SELECT cs.section_id,
                     cs.room,
                     cs.max_capacity,
                     cs.current_enrollment,
                     cs.teacher_name,
                     s.school_code,
                     s.subject_code,
                     s.name as subject_name,
                     s.grade_level
                     from class_sections cs
                      JOIN subjects s ON 
                      cs.subject_id = s.subject_id 
              WHERE  cs.school_code = $1 
                AND cs.staff_code = $2
                AND cs.is_deleted = FALSE
              AND s.is_deleted = FALSE
              ORDER BY
                  CASE
                      WHEN $2 = 'sectionId' THEN cs.section_id
                      WHEN $2 = 'subjectId' THEN cs.subject_id
                      ELSE cs.section_id
                  END
              LIMIT $3 OFFSET $4
          """)
    Flux<SubjectWithSectionProjection> findBySchoolCodeAndTeacherCode(
            String schoolCode, String staffCode, int limit, long offset);

    @Query(
            """
              SELECT COUNT(*)
              FROM class_sections cs
              JOIN subjects s ON cs.subject_id = s.subject_id 
              WHERE cs.school_code = $1 
                AND cs.staff_code = $2
                AND cs.is_deleted = FALSE
              AND s.is_deleted = FALSE
          """)
    Mono<Long> countBySchoolCodeAndTeacherCode(String schoolCode, String staffCode);


    Flux<ClassSection> findAllByClassIdAndSchoolCode(
            Long classId,
            String schoolCode);


}

