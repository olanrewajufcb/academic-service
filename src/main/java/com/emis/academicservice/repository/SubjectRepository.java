package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Subject;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubjectRepository extends R2dbcRepository<Subject, Long> {
    @Query("SELECT * FROM subjects WHERE school_id = :schoolId AND status = 'ACTIVE'")
    Flux<Subject> findBySchoolId(Long schoolId);
    
    @Query("SELECT * FROM subjects WHERE school_id = :schoolId AND subject_code = :subjectCode")
    Mono<Subject> findBySchoolIdAndCode(Long schoolId, String subjectCode);

    @Query("""
         SELECT * FROM subjects WHERE school_id = :schoolId AND class_level = :classLevel
         ORDER BY subject_id LIMIT :size OFFSET :offset
 """)
    Flux<Subject> findBySchoolIdAndClassLevel(Long schoolId, String classLevel, int size, long offset);
}

