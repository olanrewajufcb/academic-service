package com.emis.academicservice.repository;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentsInClassRow {
    private Long schoolId;
    private String schoolCode;
    private String schoolName;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private Long classId;
    private String className;
    private String gradeLevel;
    private Long formTeacherId;
    private String arm;
    private String academicYear;
}
