package com.emis.academicservice.controller;

import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.service.ClassManagementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/classes")
public class ClassManagementController {

    private final ClassManagementService service;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Arrays.stream(SchoolClass.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toSet());
    private static final String REQUEST_ID = "requestId";

    @Operation(summary = "Create a school class")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SchoolClassResponse> createSchoolClass(@Valid @RequestBody CreateSchoolClassRequest request) {
        String requestId = UUID.randomUUID().toString();

        if (!request.getAcademicYear().matches("\\d{4}/\\d{4}")) {
            throw new BadRequestException("Invalid academicYear format. Expected: '2024/2025'");
        }

        return service.createSchoolClass(request, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put(REQUEST_ID, requestId));

    }

    @Operation(summary = "Get school classes by school id")
    @GetMapping()
    public Mono<Page<SchoolClassResponse>> getSchoolClasses(
        @RequestParam String schoolCode,
        @RequestParam String academicYear,
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "page must not be less than 0")
        int page,
        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "size must be at least 1")
        int size,
        @RequestParam(defaultValue = "classId")
        String sortBy) {

            if(!ALLOWED_SORT_FIELDS.contains(sortBy)) {
                throw new BadRequestException("Invalid sort field: " + sortBy);
            }
            var pageRequest = PageRequest.of(page, size, Sort.by(sortBy));

        if (!academicYear.matches("\\d{4}/\\d{4}")) {
            throw new BadRequestException("Invalid academicYear format. Expected: '2024/2025'");
        }

        String requestId = UUID.randomUUID().toString();

        return service.getSchoolClassBySchoolCode(schoolCode, academicYear, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put(REQUEST_ID, requestId));


    }

    @GetMapping("{classId}/students")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<StudentInClassResponse>> getStudentInClassByClassId(@PathVariable Long classId,
                            @RequestParam(defaultValue = "0")
                            @Min(value = 0, message = "page must not be less than 0")
                            int page,
                            @RequestParam(defaultValue = "10")
                            @Min(value = 1, message = "size must be at least 1")
                            int size,
                            @RequestParam(defaultValue = "classId")
                            String sortBy) {


        String sortColumn = switch (sortBy) {
            case "studentName" -> "student_name";
            case "studentNumber" -> "student_number";
            case "schoolName" -> "school_name";
            default -> "student_name";
        };

        var pageRequest = PageRequest.of(page, size, Sort.by(sortColumn));

        String requestId = UUID.randomUUID().toString();

        return service.getStudentInClassByClassId(classId, pageRequest, requestId)
                .doOnSuccess(__ ->
                        ServerResponse.ok()
                                .header("Content-Encoding", "gzip")
                                .build())
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put(REQUEST_ID, requestId));


    }

}
