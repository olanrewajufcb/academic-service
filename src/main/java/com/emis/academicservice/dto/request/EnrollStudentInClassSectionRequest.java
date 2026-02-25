package com.emis.academicservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollStudentInClassSectionRequest {

    private String studentNumber;
    private String schoolCode;
}
