package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.GradeLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CreateSchoolClassRequest {
    @NotNull(message = "School id cannot be null")
    private Long schoolId;
    @NotBlank(message = "School code cannot be blank")
    private String schoolCode;
    @NotBlank(message = "School name cannot be blank")
    private String schoolName;
    private String className;
    @NotNull(message = "Grade level cannot be null")
    private GradeLevel gradeLevel;
    private String arm;
    private String stage;
    private String academicYear;
    private Long formTeacherId;
    private Integer maxStudents;
}

