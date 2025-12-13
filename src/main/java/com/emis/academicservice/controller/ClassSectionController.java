package com.emis.academicservice.controller;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.service.ClassSectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("api/v1/academic/sections")
public class ClassSectionController {

    private final ClassSectionService service;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Arrays.stream(ClassSection.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

    @PostMapping
    public Mono<ClassSectionResponse> createClassSection(@Valid @RequestBody CreateClassSectionRequest request) {
        String requestId = UUID.randomUUID().toString();

        return service.createClassSection(request, requestId)
                .doOnSubscribe(sub -> log.info("Creating class sections with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @GetMapping
    public Mono<Page<ClassSectionResponse>> getAllClassSectionsByClassId(@RequestParam Long classId,
                                                                         @RequestParam(defaultValue = "0")
    @Min(value = 0, message = "page must not be less than 0")
    int page,
                                                                         @RequestParam(defaultValue = "10")
    @Min(value = 1, message = "size must be at least 1")
    int size,
                                                                         @RequestParam(defaultValue = "sectionId")
    String sortBy) {

        if(!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        var pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        String requestId = UUID.randomUUID().toString();

        return service.getAllClassSectionsByClassId(classId, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Creating class sections with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
