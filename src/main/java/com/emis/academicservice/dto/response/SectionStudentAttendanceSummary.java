package com.emis.academicservice.dto.response;

public record SectionStudentAttendanceSummary(
        String studentNumber,
        Long totalLessons,
        Long present,
        Double attendancePercentage
) {}