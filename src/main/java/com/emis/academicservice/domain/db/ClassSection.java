package com.emis.academicservice.domain.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Data
@Table(name = "class_sections")
@AllArgsConstructor
@NoArgsConstructor
public class ClassSection {

    @Id
    private Long sectionId;
    private Long classId;
    private Long subjectId;
    private Long schoolId;
    private Long termId;
    private String schoolCode;
    private Long teacherId;
    private String staffCode;
    private String teacherName;
    private Boolean teacherValidated;
    private String room;
    private String schedule; // e.g., "Mon-Wed-Fri 10:00-11:00"
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
