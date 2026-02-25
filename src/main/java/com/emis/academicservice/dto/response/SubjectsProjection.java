package com.emis.academicservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SubjectsProjection {

    private Long sectionId;
    private Long studentId;
    private Long classId;
    private String staffCode;
    private String teacherName;
    private String subjectCode;
    private String name;
    private String gradeLevel;
    private String stage;
    private String status;
    private LocalDate enrollmentDate;

}
