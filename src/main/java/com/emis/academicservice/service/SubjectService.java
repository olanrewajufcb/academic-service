package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubjectService {

    Mono<SubjectResponse> registerSubject(RegisterSubjectRequest request, String requestId);

    Flux<SubjectResponse> getSubjectBySchoolAndClassLevel(String schoolCode, String classLevel, Pageable pageable, String requestId);
}
