package com.emis.academicservice.controller;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import com.emis.academicservice.dto.response.SubjectDto;
import com.emis.academicservice.service.SectionEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Operation(summary = "Assign subject section to student",
    description = "Assign subject section to a student")
    @PostMapping("/{sectionId}")
    public Mono<SectionEnrollmentResponse> assignSubjectSection(@PathVariable Long sectionId,
                                        @Valid @RequestBody EnrollStudentInClassSectionRequest request) {
        String requestId = UUID.randomUUID().toString();

        return service.enrollStudentInClassSection(sectionId, request, requestId)
                .doOnSubscribe(sub -> log.info("Successfully Enrolled student " +
                        " in subject section with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @Operation(summary = "Retrieve all registered subjects for a student in school",
    description = "Retrieve all  registered subjects")
    @GetMapping
    public Mono<Page<SubjectDto>> getAllStudentRegisteredSubjects(
            @RequestParam String schoolCode,
            @RequestParam String studentNumber,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must not be less than 0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            int size,
            @RequestParam(defaultValue = "classId")
            String sortBy

    ){

        String requestId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return service.getAllClassSections(schoolCode, studentNumber, pageable, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

}
