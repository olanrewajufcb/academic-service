package com.emis.academicservice.dto.request;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CreateSchoolClassRequest {
    private Long schoolId;
    private String className;
    private String classLevel;
    private String arm;
    private String academicYear;
    private Long formTeacherId;
    private Integer maxStudents;
}

