package com.emis.academicservice.dto.request;

import java.time.LocalDate;
import java.util.List;

public record StudentAttendanceRequest(
    Long sectionId,
    LocalDate attendanceDate,
    List<StudentAttendanceList> studentList
    ) {}
