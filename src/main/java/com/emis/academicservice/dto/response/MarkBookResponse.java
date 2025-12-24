package com.emis.academicservice.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MarkBookResponse {
    private Long markEntryId;
    private Long assessmentId;
    private String assessmentName;
    private Long studentId;
    private String studentName;
    private int scoreObtained;
    private int maxScore;
    private double scorePercentage;
    private String remark;
    private LocalDate markedAt;
    private String markedBy;
    private String markedByName;

}
