package com.emis.academicservice.dto.request;

import lombok.Data;

@Data
public class EnrollStudentRequest {
    private String studentNumber;
    private Long classId;
}