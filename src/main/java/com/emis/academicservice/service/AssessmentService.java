package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import reactor.core.publisher.Mono;

public interface AssessmentService {

    Mono<AssessmentResponse> createAssessment(CreateAssessmentRequest request, String requestId);
}
