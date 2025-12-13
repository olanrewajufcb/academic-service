package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassManagementService {
    Mono<SchoolClassResponse> createSchoolClass(CreateSchoolClassRequest request, String requestId);

    Mono<Page<SchoolClassResponse>> getSchoolClassBySchoolId(String schoolCode, String academicYear, Pageable pageable, String requestId);

    Mono<Page<StudentInClassResponse>> getStudentInClassByClassId(Long classId,Pageable pageable, String requestId);
}
