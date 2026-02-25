package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.StudentPromotionRequest;
import com.emis.academicservice.dto.response.StudentPromotionResponse;
import com.emis.academicservice.service.StudentPromotionService;
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
@RequestMapping("api/v1/academic/classes")
public class StudentPromotionController {

    private final StudentPromotionService studentPromotionService;

    @Operation(summary = "Promote a student to the next class",
            description = "Promote a student to the next class")
    @PostMapping("/{studentNumber}/promote")
    @ResponseStatus(HttpStatus.OK)
    public Mono<StudentPromotionResponse> promoteStudentToNextClass(
            @PathVariable String studentNumber,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid StudentPromotionRequest request) {
        log.info("Promoting student {} to the next class", studentNumber);
        String requestId = UUID.randomUUID().toString();
        return studentPromotionService
                .promoteStudent(studentNumber,idempotencyKey,  request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
