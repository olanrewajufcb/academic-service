package com.emis.academicservice.dto.response;

import lombok.Data;

@Data
public class StudentClassesResponses {

    private Long classId;
    private Long schoolId;
    private Long formTeacherId;
    private String className;
    private String gradeLevel;
    private String arm;
    private String academicYear;
    private Long studentId;
    private Long sectionId;

}
