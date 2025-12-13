package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.AssessmentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class CreateAssessmentRequest {
    @NotNull(message = "sectionId is required.")
    private Long sectionId;
    @NotNull(message = "termId is required.")
    private Long termId;
    private String name;
    private AssessmentType type;
    private String description;
    @NotNull
    @DecimalMin(value = "0.01", message = "maxScore must be > 0")
    private BigDecimal maxScore;
    @NotNull
    @DecimalMin(value = "0.00", message = "weight must be >= 0")
    @DecimalMax(value = "1.00", message = "weight must be <= 1")
    private BigDecimal weight;
    @FutureOrPresent(message = "dueDate cannot be in the past")
    private LocalDate dueDate;
}