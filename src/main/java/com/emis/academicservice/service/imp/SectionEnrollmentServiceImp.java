package com.emis.academicservice.service.imp;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import com.emis.academicservice.service.SectionEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class SectionEnrollmentServiceImp implements SectionEnrollmentService {
    @Override
    public Mono<SectionEnrollmentResponse> enrollStudentInSubjectSection(Long sectionId, CreateClassSectionRequest request, String requestId) {
        return null;
    }
}
