package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.GradeLevel;
import com.emis.academicservice.enums.SchoolStage;
import com.emis.academicservice.enums.SubjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterSubjectRequest {

    @NotNull
    @NotBlank(message = "schoolCode is required")
    private String schoolCode;
    @NotNull
    @NotBlank(message = "subjectCode is required")
    private String subjectCode;
    @NotNull
    @NotBlank(message = "subject name is required")
    private String name;
    private String description;
    @NotNull
    @NotNull(message = "grade level is required")
    private GradeLevel gradeLevel;
    @NotNull(message = "status is required")

    private SubjectStatus status;
    @NotNull(message = "stage is required")
    private SchoolStage stage;

}





