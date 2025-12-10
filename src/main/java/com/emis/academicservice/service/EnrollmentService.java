package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import reactor.core.publisher.Mono;

public interface EnrollmentService {

    Mono<EnrollmentResponse> enrollStudent(EnrollStudentRequest request, String requestId);
}
