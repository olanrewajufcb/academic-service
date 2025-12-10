package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.SubjectStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterSubjectRequest {

    private String schoolCode;
    private String subjectCode;
    private String name;
    private String description;
    private String classLevel;
    private SubjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





