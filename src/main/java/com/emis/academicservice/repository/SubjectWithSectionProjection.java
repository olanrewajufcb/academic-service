package com.emis.academicservice.repository;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectWithSectionProjection {

    private String schoolCode;
    private String subjectCode;
    private String subjectName;
    private Long sectionId;
    private String staffCode;
    private String teacherName;
    private String room;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private String gradeLevel;

}
