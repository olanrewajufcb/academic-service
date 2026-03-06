package com.emis.academicservice.controller;


import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.security.CanCreateResource;
import com.emis.academicservice.security.CanViewResource;
import com.emis.academicservice.service.SubjectService;
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
@RequestMapping("api/v1/academic")
public class SubjectController {

    private final SubjectService service;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Arrays.stream(Subject.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

    @CanCreateResource
    @Operation(summary = "Register a subject")
    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SubjectResponse> registerSubject(
            @RequestHeader(required = false) String schoolCode,
            @Valid @RequestBody RegisterSubjectRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("stage in subject ::::::: {} and schoolCode {}", request.getStage(), schoolCode);

        return service.registerSubject(request, requestId)
                .doOnSubscribe(sub -> log.info("Registering subject with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @CanViewResource
    @Operation(summary = "Get all subjects by school and grade level")
    @GetMapping("/subjects")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<SubjectResponse>> getSubjectBySchoolAndGradeLevel(@RequestParam String schoolCode,
                                                                      @RequestParam String gradeLevel, @RequestParam(defaultValue = "0")
    @Min(value = 0, message = "page must not be less than 0")
    int page,
                                                                      @RequestParam(defaultValue = "10")
    @Min(value = 1, message = "size must be at least 1")
    int size,
                                                                      @RequestParam(defaultValue = "subjectId")
    String sortBy) {

        if(!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        var pageRequest = PageRequest.of(page, size, Sort.by(sortBy));

        String requestId = UUID.randomUUID().toString();

        return service.getSubjectBySchoolAndClassLevel(schoolCode, gradeLevel,pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving subjects with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @CanViewResource
    @Operation(summary = "Get all subjects offer by school")
    @GetMapping("/subjects/all")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<SubjectResponse>> getAllSchoolSubjects(@RequestParam String schoolCode,
                                                                       @RequestParam(defaultValue = "0")
                                                                       @Min(value = 0, message = "page must not be less than 0")
                                                                       int page,
                                                                       @RequestParam(defaultValue = "10")
                                                                       @Min(value = 1, message = "size must be at least 1")
                                                                       int size,
                                                                       @RequestParam(defaultValue = "subjectId")
                                                                       String sortBy) {

        if(!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        var pageRequest = PageRequest.of(page, size, Sort.by(sortBy));

        String requestId = UUID.randomUUID().toString();

        return service.getAllSubjectsBySchoolCode(schoolCode, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving subjects with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
