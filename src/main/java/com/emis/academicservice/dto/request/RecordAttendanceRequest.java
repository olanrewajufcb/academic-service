package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordAttendanceRequest {
    private Long sectionId;
    private Long studentId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String notes;
}