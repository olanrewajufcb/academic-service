package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Assessment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface AssessmentRepository extends R2dbcRepository<Assessment, Long> {

}
