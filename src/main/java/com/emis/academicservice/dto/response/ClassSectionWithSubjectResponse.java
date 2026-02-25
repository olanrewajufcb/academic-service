package com.emis.academicservice.dto.response;

import com.emis.academicservice.repository.SubjectWithSectionProjection;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClassSectionWithSubjectResponse(
        String schoolCode,
        String subjectCode,
        String subjectName,
        Long sectionId,
        String teacherCode,
        String teacherName,
        String room,
        Integer maxCapacity,
        Integer currentEnrollment,
        String gradeLevel
) {

    public static ClassSectionWithSubjectResponse from(SubjectWithSectionProjection projection){
        return new ClassSectionWithSubjectResponse(
               projection.getSchoolCode(),
               projection.getSubjectCode(),
               projection.getSubjectName(),
               projection.getSectionId(),
               projection.getStaffCode(),
               projection.getTeacherName(),
               projection.getRoom(),
               projection.getMaxCapacity(),
               projection.getCurrentEnrollment(),
               projection.getGradeLevel()
        );
    }
}
