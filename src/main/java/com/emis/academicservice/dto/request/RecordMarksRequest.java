package com.emis.academicservice.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordMarksRequest {
    private Long assessmentId;
    private Long studentId;
    private BigDecimal scoreObtained;
    private String remark;
}
