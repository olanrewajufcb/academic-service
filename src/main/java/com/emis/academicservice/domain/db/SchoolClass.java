package com.emis.academicservice.domain.db;

import com.emis.academicservice.enums.GradeLevel;
import com.emis.academicservice.enums.SchoolStage;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Data
@Table(name = "school_classes")
public class SchoolClass {
    @Id
    private Long classId;
    private Long schoolId;
    private String schoolCode;
    private String schoolName;
    private Long formTeacherId;
    private String formTeacherName;
    private Boolean formTeacherValidated;
    private String classCode;
    private String className;
    private GradeLevel gradeLevel;
    private String arm;
    private SchoolStage stage;
    private String academicYear;
    private Integer maxStudents;
    private Integer currentStudents;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Boolean classCapacityExceeded() {
        return  maxStudents - currentStudents <= 0;
    }
}
