package com.emis.academicservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class StudentPerformanceResponse {
    private Long studentId;
    private String studentName;
    private String academicTerm;
    private Map<String, BigDecimal> subjectScores;
    private BigDecimal termAverage;
    private Integer classPosition;
    private String overallRemark;
}