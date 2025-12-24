package com.emis.academicservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarkEntryDetail(Long studentId,
                              String studentNumber,
                              String studentName,
                              String subjectName,
                              BigDecimal scoreObtained,
                              BigDecimal maxScore,
                              BigDecimal percentage,
                              String remark,
                              LocalDateTime markedAt,
                              String markedByName
){}
