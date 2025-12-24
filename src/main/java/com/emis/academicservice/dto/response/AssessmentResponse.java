package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.AssessmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssessmentResponse(
    Long assessmentId,
    String name,
    AssessmentType assessmentType,
    String description,
    BigDecimal maxScore,
    BigDecimal weight,
    LocalDate dueDate,
    Integer totalStudents,
    Integer gradedStudents,
    BigDecimal averageScore,
    LocalDateTime createdAt,
    Long createdBy,
    String createdByName
) {}