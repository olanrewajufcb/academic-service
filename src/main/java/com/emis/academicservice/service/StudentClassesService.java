package com.emis.academicservice.service;

import com.emis.academicservice.dto.response.StudentMarksResponse;
import com.emis.academicservice.repository.StudentClassesPerYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentClassesService {
    Flux<StudentClassesPerYear> getStudentsClasses (String studentNumber, String schoolCode,

                           String academicYear, Pageable pageable, String requestId);

    Mono<Page<StudentMarksResponse>> getStudentMarks (String studentNumber, String schoolCode,

                                                      String academicYear, Pageable pageable, String requestId);
}
