package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.AuditContext;
import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import com.emis.academicservice.service.StudentAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/academic")
@Validated
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService studentAttendanceService;

    @Operation(summary = "Mark a student attendance")
    @PostMapping("/schools/{schoolCode}/attendance/mark")
    @ResponseStatus(HttpStatus.OK)
    public Flux<StudentAttendanceResponse> markAttendance(
            @PathVariable String schoolCode,
            @RequestBody StudentAttendanceRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return studentAttendanceService.markStudentAttendance(request, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("audit",
                        new AuditContext("1234L", "Subject_Teacher", "WEB")));
    }
}
