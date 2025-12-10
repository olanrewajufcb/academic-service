package com.emis.academicservice.domain.db;

import com.emis.academicservice.enums.AssessmentType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "assessments")
public class Assessment {
    @Id
    private Long assessmentId;
    private Long sectionId;
    private String name;
    private AssessmentType type;
    private String description;
    private BigDecimal maxScore;
    private BigDecimal weight;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}