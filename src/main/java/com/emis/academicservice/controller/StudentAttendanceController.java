package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.AuditContext;
import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.SectionAttendanceReportResponse;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import com.emis.academicservice.dto.response.StudentAttendanceSummaryResponse;
import com.emis.academicservice.service.StudentAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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


    @Operation(summary = "Compute a student's attendance summary for a school")
    @GetMapping("/schools/{schoolCode}/students/{studentNumber}/attendance-summary")
    @ResponseStatus(HttpStatus.OK)
    public Mono<StudentAttendanceSummaryResponse> computeStudentAttendanceSummary(
            @PathVariable String schoolCode,
            @PathVariable String studentNumber,
            @RequestParam Long termId
    ) {
        String requestId = UUID.randomUUID().toString();
        return studentAttendanceService.computeStudentAttendanceSummary(
                schoolCode, studentNumber, termId, requestId)
                .contextWrite(ctx -> ctx.put("audit",
                        new AuditContext("1234L", "Subject_Teacher", "WEB")));
    }


    @Operation(summary = "Get a section's attendance report")
    @GetMapping("/schools/{schoolCode}/sections/{sectionId}/attendance-report")
    @ResponseStatus(HttpStatus.OK)
    public Mono<SectionAttendanceReportResponse> getSectionAttendanceReport(
            @PathVariable String schoolCode,
            @PathVariable Long sectionId,
            @RequestParam Long termId
    ) {
        String requestId = UUID.randomUUID().toString();
        return studentAttendanceService.getSectionAttendanceReport(
                schoolCode, sectionId, termId, requestId)
                .contextWrite(ctx -> ctx.put("audit",
                        new AuditContext("1234L", "Subject_Teacher", "WEB")));
    }
}
