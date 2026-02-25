package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.StudentPromotionRequest;
import com.emis.academicservice.dto.response.StudentPromotionResponse;
import reactor.core.publisher.Mono;

public interface StudentPromotionService {
    Mono<StudentPromotionResponse> promoteStudent(String studentNumber,
                                                  String idempotencyKey,
                                                  StudentPromotionRequest request,
                                                  String requestId);
}
