package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.AssessmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssessmentResponse {
    private Long assessmentId;
    private Long sectionId;
    private String name;
    private AssessmentType type;
    private String description;
    private BigDecimal maxScore;
    private BigDecimal weight;
    private LocalDate dueDate;
}
