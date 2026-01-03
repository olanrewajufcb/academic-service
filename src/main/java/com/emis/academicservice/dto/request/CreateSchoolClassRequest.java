package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.GradeLevel;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CreateSchoolClassRequest {
    private String schoolCode;
    private String schoolName;
    private String className;
    private GradeLevel gradeLevel;
    private String arm;
    private String stage;
    private String academicYear;
    private Long formTeacherId;
    private Integer maxStudents;
}

