package com.emis.academicservice.service;

import com.emis.academicservice.dto.response.StudentEnrollmentResponse;
import reactor.core.publisher.Mono;

public interface ActiveStudentEnrollmentCache {

    Mono<StudentEnrollmentResponse> getStudentEnrollmentFromCache(
            String schoolCode, String studentNumber,  String academicYear);
}
