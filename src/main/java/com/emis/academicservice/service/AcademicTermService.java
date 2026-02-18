package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateAcademicTermRequest;
import com.emis.academicservice.dto.response.AcademicTermResponse;
import reactor.core.publisher.Mono;

public interface AcademicTermService {
    Mono<AcademicTermResponse> createAcademicTerm(CreateAcademicTermRequest request,
                                                  String schoolCode, String requestId);

    Mono<AcademicTermResponse> getAcademicTerm(String schoolCode,
                                               Long academicTermId,
                                               String requestId);
}
