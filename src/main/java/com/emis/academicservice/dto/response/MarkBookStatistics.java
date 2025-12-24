package com.emis.academicservice.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record MarkBookStatistics(
    BigDecimal averageScore,
    BigDecimal highestScore,
    BigDecimal lowestScore,
    int gradedStudents,
    int totalStudents,
    int pendingGrading,
    Map<String, Long> gradeDistribution  // Nigerian grading: A, B, C, D, F
) {
    public static MarkBookStatistics empty() {
        return new MarkBookStatistics(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0, 0, 0,
            Map.of()
        );
    }
}
