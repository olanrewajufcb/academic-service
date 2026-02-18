package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentAttendanceService {

    Flux<StudentAttendanceResponse> markStudentAttendance(
            StudentAttendanceRequest request, String schoolCode, String requestId);
}
