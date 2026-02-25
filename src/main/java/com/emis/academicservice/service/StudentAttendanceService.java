package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.StudentAttendanceRequest;
import com.emis.academicservice.dto.response.SectionAttendanceReportResponse;
import com.emis.academicservice.dto.response.StudentAttendanceResponse;
import com.emis.academicservice.dto.response.StudentAttendanceSummaryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentAttendanceService {

    Flux<StudentAttendanceResponse> markStudentAttendance(
            StudentAttendanceRequest request, String schoolCode, String requestId);

    Mono<StudentAttendanceSummaryResponse> computeStudentAttendanceSummary(
            String schoolCode, String studentNumber, Long termId, String requestId);

    Mono<SectionAttendanceReportResponse> getSectionAttendanceReport(
            String schoolCode,  Long sectionId, Long termId, String requestId);
}
