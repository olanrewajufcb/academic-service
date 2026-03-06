package com.emis.academicservice.controller;


import com.emis.academicservice.dto.request.CreateAcademicTermRequest;
import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AcademicTermResponse;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.security.CanAccessRestrictedResource;
import com.emis.academicservice.security.CanCreateResource;
import com.emis.academicservice.service.AcademicTermService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic")
public class AcademicTermController {

    private final AcademicTermService service;

    @CanCreateResource
    @Operation(summary = "Create a Academic Term for a school")
    @PostMapping("/schools/{schoolCode}/academic-term")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AcademicTermResponse> createAcademicTerm(
            @PathVariable String schoolCode,
            @Valid @RequestBody CreateAcademicTermRequest request
            ) {

        String requestId = UUID.randomUUID().toString();

        return service.createAcademicTerm(request, schoolCode, requestId)
                .doOnSubscribe(sub -> log.info("Creating AcademicTerm with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve a created academic term for a school")
    @GetMapping("/schools/{schoolCode}/academic-term/{academicTermId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AcademicTermResponse> getAcademicTerm(
            @PathVariable String schoolCode,
            @PathVariable Long academicTermId) {

        String requestId = UUID.randomUUID().toString();

        return service.getAcademicTerm(schoolCode, academicTermId, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving AcademicTerm with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
