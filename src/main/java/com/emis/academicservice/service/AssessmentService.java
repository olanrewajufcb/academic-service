package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.dto.response.SectionAssessmentsResponse;
import com.emis.academicservice.enums.AssessmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface AssessmentService {

    Mono<AssessmentResponse> createAssessment(CreateAssessmentRequest request, String requestId);

    Mono<Page<AssessmentResponse>> getAllAssessmentsForClassSection(Long sectionId, String schoolCode,
                                         AssessmentType assessmentType, String term, Pageable pageable, String requestId);
}
