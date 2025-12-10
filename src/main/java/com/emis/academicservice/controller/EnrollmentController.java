package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    @PostMapping
    public Mono<EnrollmentResponse> enrollStudentToClass(@Valid @RequestBody EnrollStudentRequest request){
        String requestId = UUID.randomUUID().toString();

        return service.enrollStudent(request,requestId)
                .doOnNext(enrollment -> log.info(String.format("Enrollment with id %s succesfully", requestId)))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
