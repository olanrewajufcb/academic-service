package com.emis.academicservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchoolClassResponse {
    private Long classId;
    private String className;
    private String gradeLevel;
    private String arm;
    private String academicYear;
    private Long formTeacherId;
    private String formTeacherName;
    private Integer currentStudents;
    private Integer maxStudents;
    private LocalDateTime createdAt;
}