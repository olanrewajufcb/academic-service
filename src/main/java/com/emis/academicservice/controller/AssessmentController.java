package com.emis.academicservice.controller;


import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.service.AssessmentService;
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
@RequestMapping("api/v1/academic/assessments")
public class AssessmentController {

    private final AssessmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {

        String requestId = UUID.randomUUID().toString();

        return service.createAssessment(request, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
