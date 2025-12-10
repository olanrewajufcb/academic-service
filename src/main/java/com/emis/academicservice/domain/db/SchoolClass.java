package com.emis.academicservice.domain.db;

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
    private Long formTeacherId;
    private String className;
    private String classLevel;
    private String arm;
    private String stage;
    private String academicYear;
    private Integer maxStudents;
    private Integer currentStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
