package com.emis.academicservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentMarksResponse(
    Long termId,
    LocalDate startDate,
    LocalDate endDate,
    Long studentId,
    Long sectionId,
    Long subjectId,
    BigDecimal totalScore,
    BigDecimal averageScore,
    Integer positionInClass,
    String remarks,
    String subjectName,
    String teacherName
) {}
