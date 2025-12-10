package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.ClassSection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
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
}

