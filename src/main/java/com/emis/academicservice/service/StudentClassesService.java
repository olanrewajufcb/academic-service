package com.emis.academicservice.service;

import com.emis.academicservice.repository.StudentClassesPerYear;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

public interface StudentClassesService {
    Flux<StudentClassesPerYear> getStudentsClasses (String studentNumber, String academicYear, Pageable pageable, String requestId);
}
