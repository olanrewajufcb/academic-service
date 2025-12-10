package com.emis.academicservice.controller;

import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.StudentClassesResponses;
import com.emis.academicservice.repository.StudentClassesPerYear;
import com.emis.academicservice.service.StudentClassesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/academic/students/{studentNumber}/classes")
public class StudentClassesController {

    private final StudentClassesService service;
    @GetMapping
    public Flux<StudentClassesPerYear> getAllStudentClasses(@PathVariable String studentNumber,
                                                            @RequestParam String academicYear) {
        String requestId = UUID.randomUUID().toString();

        return service.getStudentsClasses(studentNumber,academicYear,requestId)
                .doOnSubscribe(sub -> log.info("Successfully retrieved students classes for the academic year"))
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
