package com.emis.academicservice.controller;


import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.dto.response.SectionAssessmentsResponse;
import com.emis.academicservice.enums.AssessmentType;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.service.AssessmentService;
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
public class AssessmentController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Arrays.stream(Assessment.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

    private final AssessmentService service;

    @PostMapping("/schools/{schoolCode}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AssessmentResponse> createAssessment(
            @Valid @RequestBody CreateAssessmentRequest request,
            @PathVariable String schoolCode) {

        String requestId = UUID.randomUUID().toString();

        return service.createAssessment(request, schoolCode, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @GetMapping("/schools/{schoolCode}/sections/{sectionId}/assessments")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<AssessmentResponse>> getSectionAssessment( @PathVariable Long sectionId,
                                                                @PathVariable String schoolCode,
                                                                @RequestParam AssessmentType assessmentType,
                                                                @RequestParam String term,
                                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "10") @Min(1) int size,
                                                                @RequestParam(defaultValue = "dueDate") String sortBy,
                                                                @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return Mono.error(new BadRequestException(
                    String.format("Invalid sort field: %s. Allowed: %s",
                            sortBy, ALLOWED_SORT_FIELDS)));
        }

        var pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        String requestId = UUID.randomUUID().toString();

        return service.getAllAssessmentsForClassSection(sectionId, schoolCode, assessmentType,
                        term, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
