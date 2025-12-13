package com.emis.academicservice.repository;

public interface SchoolClassProjection {
    Long getClassId();
    Long getSchoolId();
    String getSchoolCode();
    String getAcademicYear();
    String getClassName();
    String getClassLevel();
}