package com.emis.academicservice.controller;

import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.service.ClassManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SchoolClassResponse> createSchoolClass(@Valid @RequestBody CreateSchoolClassRequest request) {
        String requestId = UUID.randomUUID().toString();

        return service.createSchoolClass(request, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @GetMapping()
    public Flux<SchoolClassResponse> getSchoolClassBySchoolId(
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

        String requestId = UUID.randomUUID().toString();

        return service.getSchoolClassBySchoolId(schoolCode, academicYear,pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @GetMapping("{classId}/students")
    public Flux<StudentInClassResponse> getStudentInClassByClassId(@PathVariable Long classId,
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

        String requestId = UUID.randomUUID().toString();

        return service.getStudentInClassByClassId(classId, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));


    }

}
