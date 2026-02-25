package com.emis.academicservice.domain.db;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "academic_term")
@Builder
public class AcademicTerm {
    @Id
    private Long termId;
    private Long schoolId;
    private String termCode;
    private String name;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}