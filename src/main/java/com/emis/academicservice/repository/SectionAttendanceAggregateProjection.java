package com.emis.academicservice.repository;

public interface SectionAttendanceAggregateProjection {
    Long getTotalLessons();
    Long getLessonsWithAttendance();
    Long getLessonsWithoutAttendance();
}

