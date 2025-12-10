package com.emis.academicservice.dto.response;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectResponse {
    private Long subjectId;
    private String subjectCode;
    private String name;
    private String description;
    private String classLevel;
    private String status;
    private LocalDateTime createdAt;
}