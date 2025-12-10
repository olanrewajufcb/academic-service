package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.SchoolLevel;
import com.emis.academicservice.enums.SchoolType;
import lombok.Data;

@Data
public class StudentInClassResponse {
    private String name;
    private SchoolType type;
    private SchoolLevel level;
    private Long classId;
    private Long schoolId;
    private Long formTeacherId;
    private String className;
    private String classLevel;
    private String arm;
    private String academicYear;

}
