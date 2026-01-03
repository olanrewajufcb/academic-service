package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.GradeLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchoolClassResponse {
    private Long classId;
    private String className;
    private GradeLevel gradeLevel;
    private String arm;
    private String academicYear;
    private Long formTeacherId;
    private String formTeacherName;
    private Integer currentStudents;
    private Integer maxStudents;
    private LocalDateTime createdAt;
}