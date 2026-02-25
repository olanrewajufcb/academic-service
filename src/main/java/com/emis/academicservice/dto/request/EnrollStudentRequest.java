package com.emis.academicservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollStudentRequest {
    @NotBlank(message = "schoolCode is required")
    private String schoolCode;
    @NotBlank(message = "studentNumber is required")
    private String studentNumber;
    @NotNull(message = "classId is required")
    private Long classId;
}