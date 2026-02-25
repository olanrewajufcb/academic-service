package com.emis.academicservice.repository;


import lombok.Data;

@Data
public class SectionValidationProjection {
    private Long sectionId;
    private Long classId;
    private Long schoolId;
    private String schoolCode;
    private String academicYear;

}