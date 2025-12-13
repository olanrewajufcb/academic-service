package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EnrollmentResponse> enrollStudentToClass(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EnrollStudentRequest request){

        return service.enrollStudent(request, idempotencyKey);
    }
}
