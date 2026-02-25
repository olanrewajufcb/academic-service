package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;

import com.emis.academicservice.dto.response.SubjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;


public interface SectionEnrollmentService {

    Mono<SectionEnrollmentResponse> enrollStudentInClassSection(Long sectionId,
                                        EnrollStudentInClassSectionRequest request, String requestId);

    Mono<Page<SubjectDto>> getAllClassSections(
            String schoolCode, String studentNumber,
            Pageable pageable, String requestId);


    Mono<SubjectDto> removeStudentFromSection(Long sectionId, Long studentId);
}
