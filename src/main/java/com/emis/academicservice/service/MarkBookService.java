package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.RecordAssessmentRequest;
import com.emis.academicservice.dto.response.MarkBookResponse;
import com.emis.academicservice.dto.response.MarkBookViewResponse;
import reactor.core.publisher.Mono;

public interface MarkBookService {
    Mono<MarkBookResponse> recordAssessmentMark(RecordAssessmentRequest request, String requestId);
    Mono<MarkBookViewResponse> getSectionMarkBook(Long sectionId, String schoolCode, Long assessmentId,  String academicYear, String requestId);
}
