package com.emis.academicservice.repository;

public interface StudentsInClassRow {
    Long getSchoolId();
    String getSchoolCode();        // e.g., "PUBLIC"
    String getSchoolName();
    Long getStudentId();
    String getStudentNumber();
    String getStudentName();
    Long getClassId();
    String getClassName();
    String getClassLevel();        // e.g., "JSS_1"
    Long getFormTeacherId();
    String getArm();
    String getAcademicYear();
}
