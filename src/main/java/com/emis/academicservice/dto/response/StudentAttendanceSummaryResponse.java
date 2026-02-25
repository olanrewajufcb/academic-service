package com.emis.academicservice.dto.response;

public record StudentAttendanceSummaryResponse(
        String studentNumber,
        Long termId,
        Long totalLessons,
        Long present,
        Long absent,
        Long late,
        Double attendancePercentage
) {}