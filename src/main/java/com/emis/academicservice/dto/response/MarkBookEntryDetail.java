package com.emis.academicservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarkBookEntryDetail(
        Long markEntryId,
        Long assessmentId,
        Long studentId,
        BigDecimal scoreObtained,
        BigDecimal scorePercentage,
        String remark,
        LocalDateTime markedAt,
        BigDecimal maxScore,
        String assessmentName,
        Long subjectId,
        String academicTerm,
        String subjectName
    ) {}