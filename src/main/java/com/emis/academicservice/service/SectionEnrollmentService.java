package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;

import reactor.core.publisher.Mono;


public interface SectionEnrollmentService {

    Mono<SectionEnrollmentResponse> enrollStudentInClassSection(Long sectionId,
                                        EnrollStudentInClassSectionRequest request, String requestId);
}
