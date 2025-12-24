package com.emis.academicservice.service.client;

import com.emis.academicservice.dto.response.StudentDetailsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StudentClientService {
    Mono<StudentDetailsResponse> getStudentDetails(String studentNumber, String schoolCode);

    Flux<StudentDetailsResponse> getStudentDetails(Long studentId);

    Flux<StudentDetailsResponse> getStudentDetailsBatch(List<Long> studentIds);

    Mono<StudentDetailsResponse> getStudentByIdAndSchoolId(Long studentId, String schoolCode);

}
