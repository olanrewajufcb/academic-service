package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassSectionService {

    Mono<ClassSectionResponse> createClassSection(CreateClassSectionRequest request, String requestId);
    Flux<ClassSectionResponse> getAllClassSectionsByClassId(Long classId, Pageable pageable, String requestId);

}
