package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;

import reactor.core.publisher.Mono;


public interface SectionEnrollmentService {

    Mono<SectionEnrollmentResponse> enrollStudentInSubjectSection(Long sectionId,
                             CreateClassSectionRequest request, String requestId);
}
