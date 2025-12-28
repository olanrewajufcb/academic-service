package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.response.SubjectName;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SubjectRepository extends R2dbcRepository<Subject, Long> {
    @Query("SELECT * FROM subjects WHERE school_id = :schoolId AND status = 'ACTIVE'")
    Flux<Subject> findBySchoolId(Long schoolId);
    
    @Query("SELECT * FROM subjects WHERE school_id = :schoolId AND subject_code = :subjectCode")
    Mono<Subject> findBySchoolIdAndCode(Long schoolId, String subjectCode);

    @Query("""
         SELECT * FROM subjects WHERE school_id = :schoolId AND grade_level = :gradeLevel
         ORDER BY subject_id LIMIT :size OFFSET :offset
 """)
    Flux<Subject> findBySchoolIdAndGradeLevel(Long schoolId, String gradeLevel, int size, long offset);

    @Query("SELECT COUNT(*) FROM subjects WHERE school_id = :schoolId AND grade_level = :gradeLevel")
    Mono<Long> countBySchoolIdAndClassLevel(
            @Param("schoolId") Long schoolId,
            @Param("gradeLevel") String gradeLevel
    );

    @Query("SELECT subject_id, name FROM subjects WHERE subject_id IN (:ids)")
    Flux<SubjectName> findNamesByIds(List<Long> ids);
}

