package com.emis.academicservice.repository;

public interface SectionStudentAttendanceProjection {
    String getStudentNumber();
    Long getTotalLessons();
    Long getPresent();
}