package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.AttendanceStatus;

public record StudentAttendanceList(
        String studentNumber,
        AttendanceStatus status,
        String notes
) {}
