package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import com.emis.academicservice.service.SectionEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/sections")
public class SectionEnrollmentController {

    private final SectionEnrollmentService service;

    @PostMapping("/{sectionId}")
    public Mono<SectionEnrollmentResponse> enrollStudentInClassSection(@PathVariable Long sectionId,
                                        @Valid @RequestBody EnrollStudentInClassSectionRequest request) {
        String requestId = UUID.randomUUID().toString();

        return service.enrollStudentInClassSection(sectionId, request, requestId)
                .doOnSubscribe(sub -> log.info("Successfully Enrolled student " +
                        " in subject section with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

}
