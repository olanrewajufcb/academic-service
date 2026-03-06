package com.emis.academicservice.controller;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.request.CreateLessonRequest;
import com.emis.academicservice.dto.request.StaffUpdateRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.ClassSectionWithSubjectResponse;
import com.emis.academicservice.dto.response.LessonResponse;
import com.emis.academicservice.exception.BadRequestException;
import com.emis.academicservice.security.CanCreateResource;
import com.emis.academicservice.security.CanViewResource;
import com.emis.academicservice.service.ClassSectionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class ClassSectionController {

    private final ClassSectionService service;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Arrays.stream(ClassSection.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

    @CanCreateResource
    @Operation(summary = "Create a new class section with a school code")
    @PostMapping("/schools/{schoolCode}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ClassSectionResponse> createClassSection(
            @PathVariable String schoolCode,
            @Valid @RequestBody CreateClassSectionRequest request
           ) {
        String requestId = UUID.randomUUID().toString();

        return service.createClassSection(request, schoolCode, requestId)
                .doOnSubscribe(sub -> log.info("Creating class sections with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @CanViewResource
    @Operation(summary = "Get all sections/subjects for a given class")
    @GetMapping("/schools/{schoolCode}/classes/{classId}/sections")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByClassId(
            @PathVariable String schoolCode,
            @PathVariable Long classId,
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

        return service.getAllClassSectionsByClassId(classId, schoolCode, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Creating class sections with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanViewResource
    @Operation(summary = "Get all class sections/subjects for a given class taught by a staff")
    @GetMapping("/schools/{schoolCode}/classes/{classId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ClassSectionResponse> getAllClassSectionsByClassIdAndStaffCode(
            @PathVariable String schoolCode,
            @PathVariable Long classId,
            @RequestParam String staffCode
    ){
        String requestId = UUID.randomUUID().toString();
        return service.getClassSectionsByClassIdAndStaffCode(classId, schoolCode, staffCode, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving the class section with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanCreateResource
    @Operation(summary = "Update a class section with the staff code in a given school")
    @PutMapping("/schools/{schoolCode}/classes/{classId}/sections/{sectionId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ClassSectionResponse> updateClassSection(
            @PathVariable String schoolCode,
            @PathVariable Long classId,
            @PathVariable Long sectionId,
            @Valid @RequestBody StaffUpdateRequest request) {
        String requestId = UUID.randomUUID().toString();
        return service.updateClassSection(classId, sectionId, schoolCode, request, requestId)
                .doOnSubscribe(sub -> log.info("Updating class section with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
            }


    @CanCreateResource
    @Operation(summary = "Create a lesson for a given section",
    description = "Create a lesson for a given subject ")
    @PostMapping("/schools/{schoolCode}/sections/{sectionId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LessonResponse> createLesson(
            @PathVariable String schoolCode,
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateLessonRequest request) {
        String requestId = UUID.randomUUID().toString();
        return service.createLesson(schoolCode, sectionId, request, requestId)
                .doOnSubscribe(sub -> log.info("Creating lesson with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanViewResource
    @Operation(summary = "Get all subjects taught by a staff in a school")
    @GetMapping("/schools/{schoolCode}/staff/{staffCode}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByClassIdAndStaffCode(
            @PathVariable String schoolCode,
            @PathVariable String staffCode,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must not be less than 0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            int size,
            @RequestParam(defaultValue = "sectionId")
            String sortBy
    ){
        String requestId = UUID.randomUUID().toString();

        Pageable pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        return service.getAllClassSectionsByStaffCode(schoolCode, staffCode, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Retrieving the class section with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

}
