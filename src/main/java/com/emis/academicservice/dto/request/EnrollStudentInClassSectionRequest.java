package com.emis.academicservice.dto.request;

import lombok.Data;

@Data
public class EnrollStudentInClassSectionRequest {

    private String studentNumber;
    private String schoolCode;
    private String academicYear;
}
