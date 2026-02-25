package com.emis.academicservice.dto.request;

import java.util.List;

public record StudentAttendanceRequest(
    Long lessonId,
    List<StudentAttendanceList> studentList
    ) {}
