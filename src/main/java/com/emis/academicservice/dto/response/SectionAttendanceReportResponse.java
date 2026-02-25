package com.emis.academicservice.dto.response;

import java.util.List;

public record SectionAttendanceReportResponse(
        Long sectionId,
        Long termId,
        Long totalLessonsHeld,
        Long lessonsWithAttendanceMarked,
        Long LessonsWithoutAttendanceMarked,
        Long totalAttendanceRecords,
        Double averageAttendanceRate,
        List<SectionStudentAttendanceSummary> studentBreakdown
) {}