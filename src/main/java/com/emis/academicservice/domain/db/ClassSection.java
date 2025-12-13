package com.emis.academicservice.domain.db;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Data
@Table(name = "class_sections")
public class ClassSection {

    @Id
    private Long schoolId;
    private Long sectionId;
    private Long classId;
    private Long subjectId;
    private Long teacherId;
    private String room;
    private String schedule; // e.g., "Mon-Wed-Fri 10:00-11:00"
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
