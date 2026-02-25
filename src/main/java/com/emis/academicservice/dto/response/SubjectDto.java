package com.emis.academicservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubjectDto(
    Long sectionId,
    Long studentId,
    Long classId,
    String staffCode,
    String teacherName,
    String subjectCode,
    String name,
    String gradeLevel,
    String stage,
    String status,
    LocalDate enrollmentDate

    ) {
    public static SubjectDto from(SubjectsProjection projection){
        return new SubjectDto(
            projection.getSectionId(),
            projection.getStudentId(),
            projection.getClassId(),
            projection.getStaffCode(),
            projection.getTeacherName(),
            projection.getSubjectCode(),
            projection.getName(),
            projection.getGradeLevel(),
            projection.getStage(),
            projection.getStatus(),
            projection.getEnrollmentDate()
        );
    }
}
