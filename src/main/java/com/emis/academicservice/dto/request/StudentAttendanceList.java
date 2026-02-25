package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.AttendanceStatus;

public record StudentAttendanceList(
        String studentNumber,
        String studentName,
        AttendanceStatus status,
        String notes
) {}
