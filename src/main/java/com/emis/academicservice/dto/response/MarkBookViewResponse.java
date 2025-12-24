package com.emis.academicservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MarkBookViewResponse(Long sectionId,
                                   Long assessmentId,
                                   String assessmentName,
                                   String academicTerm,
                                   List<MarkEntryDetail> marks,
                                   BigDecimal averageScore,
                                   BigDecimal highestScore,
                                   BigDecimal lowestScore,
                                   int totalStudents,
                                   int gradedStudents,
                                   int pendingGrading,
                                   LocalDateTime generatedAt) {}
