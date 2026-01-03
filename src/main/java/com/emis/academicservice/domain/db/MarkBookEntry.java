package com.emis.academicservice.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "markbook_entry")
public class MarkBookEntry {
    @Id
    private Long markEntryId;
    private Long assessmentId;
    private Long studentId;
    private Long termId;
    private BigDecimal scoreObtained;
    private BigDecimal scorePercentage;
    private String remark;
    private LocalDateTime markedAt;
    private Long markedBy;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;

}
