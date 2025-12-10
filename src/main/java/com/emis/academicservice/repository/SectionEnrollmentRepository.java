package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.SectionEnrollment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;


public interface SectionEnrollmentRepository extends R2dbcRepository<SectionEnrollment, Long> {


}
