package com.emis.academicservice.controller;

import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.StudentClassesResponses;
import com.emis.academicservice.dto.response.StudentMarksResponse;
import com.emis.academicservice.repository.StudentClassesPerYear;
import com.emis.academicservice.service.StudentClassesService;
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

import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/students/{studentNumber}")
public class StudentClassesController {

    private final StudentClassesService service;
    @GetMapping("/classes")
    public Flux<StudentClassesPerYear> getAllStudentClasses(@PathVariable String studentNumber,
                  @RequestParam String schoolCode,
                  @RequestParam String academicYear, @RequestParam(defaultValue = "0")
                  @Min(value = 0, message = "page must not be less than 0")
                  int page,
                  @RequestParam(defaultValue = "10")
                  @Min(value = 1, message = "size must be at least 1")
                  int size,
                  @RequestParam(defaultValue = "class_name")
                  String sortBy) {

        String requestId = UUID.randomUUID().toString();
        var pageRequest = PageRequest.of(page, size, Sort.by(sortBy));


        return service.getStudentsClasses(studentNumber,schoolCode, academicYear,pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Successfully retrieved students classes for the academic year"))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @GetMapping("/marks")
    public Mono<Page<StudentMarksResponse>> getStudentMarks(@PathVariable String studentNumber,
                                                            @RequestParam String schoolCode,
                                                            @RequestParam String academicYear,
                                                            @RequestParam(defaultValue = "0")
                                                            @Min(value = 0, message = "page must not be less than 0")
                                                            int page,
                                                            @RequestParam(defaultValue = "10")
                                                            @Min(1) int size,
                                                            @RequestParam(defaultValue = "averageScore,desc")
                                                            String sortBy
    ){
        String requestId = UUID.randomUUID().toString();
        String[] sortParts = sortBy.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.DESC;

        var pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        return service.getStudentMarks(studentNumber, schoolCode, academicYear, pageRequest, requestId)
                .doOnSubscribe(sub -> log.info("Successfully retrieved students marks for the academic year"))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
