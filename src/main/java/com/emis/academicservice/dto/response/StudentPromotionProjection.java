package com.emis.academicservice.dto.response;

import lombok.Data;

@Data
public class StudentPromotionProjection {
    private String studentNumber;
    private String schoolCode;
    private String academicYear;
    private Long classId;
    private String className;
    private String sectionName;
    private String sectionCode;
    private String subjectName;
    private String gradeLevel;
}
