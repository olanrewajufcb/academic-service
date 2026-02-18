package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.service.EnrollmentService;
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
@RequestMapping("api/v1/academic/placements")
public class EnrollmentController {

    private final EnrollmentService service;

    @Operation(summary = "Place a student in a class")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EnrollmentResponse> placeStudentInClass(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EnrollStudentRequest request){

        return service.placeStudentInClass(request, idempotencyKey);
    }

    @Operation(summary = "Retrieve a student placement from a given class")
    @GetMapping("/{classId}/{studentNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<EnrollmentResponse> getStudentPlacement(
            @PathVariable Long classId,
            @PathVariable String studentNumber,
            @RequestParam String schoolCode){

        String requestId = UUID.randomUUID().toString();
        return service.getStudentPlacement(classId, studentNumber, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
