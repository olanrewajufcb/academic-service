package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.SchoolLevel;
import com.emis.academicservice.enums.SchoolType;
import lombok.Data;

@Data
public class StudentInClassResponse {
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private Long schoolId;
    private String schoolName;
    private SchoolType type;
    private SchoolLevel level;
    private Long classId;
    private String className;
    private String classLevel;
    private Long formTeacherId;
    private String arm;
    private String academicYear;

}
