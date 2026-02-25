package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import reactor.core.publisher.Mono;

public interface EnrollmentService {

    Mono<EnrollmentResponse> placeStudentInClass(EnrollStudentRequest request, String idempotencyKey);

    Mono<EnrollmentResponse> getStudentPlacement(Long classId,
                     String studentNumber, String schoolCode, String requestId);

    Mono<EnrollmentResponse> removeStudentFromClass(Long classId,
                     String studentNumber, String schoolCode);
}
