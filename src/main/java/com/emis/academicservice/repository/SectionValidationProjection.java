package com.emis.academicservice.repository;

public interface SectionValidationProjection {
    Long getSectionId();
    Long getClassId();
    Long getSchoolId();
    String getSchoolCode();
}