package com.emis.academicservice.controller;


import com.emis.academicservice.dto.request.RecordAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import com.emis.academicservice.dto.response.MarkBookResponse;
import com.emis.academicservice.dto.response.MarkBookViewResponse;
import com.emis.academicservice.service.MarkBookService;
import jakarta.validation.Valid;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/academic")
public class MarkBookController {

    private final MarkBookService service;

    @PostMapping("/markbook/enteries")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MarkBookResponse> recordAssessmentMark(@Valid @RequestBody RecordAssessmentRequest request) {

        String requestId = UUID.randomUUID().toString();

        return service.recordAssessmentMark(request, requestId)
                .doOnSubscribe(sub -> log.info("Creating school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @GetMapping("/sections/{sectionId}/markbook")
    public Mono<MarkBookViewResponse> getSectionMarkBook(
            @PathVariable Long sectionId,
            @RequestParam String schoolCode,
            @RequestParam Long assessmentId,
            @RequestParam(required = false) String academicYear
    ){
        String requestId = UUID.randomUUID().toString();
        return service.getSectionMarkBook(sectionId, schoolCode, assessmentId, academicYear, requestId)
                .doOnSubscribe(sub -> log.info("Getting school class with id {}", requestId))
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

}
