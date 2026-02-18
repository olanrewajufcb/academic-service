package com.emis.academicservice.dto.response;

import com.emis.academicservice.domain.db.StudentAttendance;

public record StudentAttendanceResponse(
    String studentNumber,
    String studentName,
    Long sectionId,
    String sectionName,
    String academicTerm,
    Integer totalDays,
    Integer presentDays,
    Integer absentDays,
    Integer lateDays,
    Double attendanceRate
    ) {
    public static StudentAttendanceResponse from(StudentAttendance studentAttendance) {
        return new StudentAttendanceResponse(
                studentAttendance.getStudentNumber(),
                null,
                studentAttendance.getSectionId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null

        );
    }
}
