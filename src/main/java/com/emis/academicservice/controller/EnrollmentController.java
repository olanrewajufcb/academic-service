package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import com.emis.academicservice.security.CanCreateResource;
import com.emis.academicservice.security.CanViewResource;
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
@RequestMapping("api/v1/academic")
public class EnrollmentController {

    private final EnrollmentService service;

    @CanCreateResource
    @Operation(summary = "Place a student in a class")
    @PostMapping("/placements")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EnrollmentResponse> placeStudentInClass(
            @RequestHeader(required = false) String schoolCode,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EnrollStudentRequest request){
        log.info("Placing student in class with school code {}", schoolCode);
        return service.placeStudentInClass(request, idempotencyKey);
    }

    @CanViewResource
    @Operation(summary = "Retrieve a student placement from a given class")
    @GetMapping("/placements/{classId}/{studentNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<EnrollmentResponse> getStudentPlacement(
            @RequestParam String schoolCode,
            @PathVariable Long classId,
            @PathVariable String studentNumber
            ){

        String requestId = UUID.randomUUID().toString();
        return service.getStudentPlacement(classId, studentNumber, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
