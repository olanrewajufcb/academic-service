package com.emis.academicservice.domain.db;


import com.emis.academicservice.enums.GradeLevel;
import com.emis.academicservice.enums.SchoolStage;
import com.emis.academicservice.enums.SubjectStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Builder
@Data
@Table(name = "subjects")
public class Subject {

    @Id
    private Long subjectId;
    private Long schoolId;
    private String subjectCode;
    private String name;
    private String description;
    private GradeLevel gradeLevel;
    private SchoolStage stage;
    private SubjectStatus status;
    private Boolean schoolValidated;
    private LocalDateTime lastSchoolValidation;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
