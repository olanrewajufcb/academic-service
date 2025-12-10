package com.emis.academicservice.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "term_scores")
public class TermScores {
    @Id
    private Long termScoreId;
    private Long studentId;
    private Long sectionId;
    private String academicTerm;
    private BigDecimal totalScore;
    private BigDecimal averageScore;
    private Integer positionInClass;
    private String remarks;
    private LocalDateTime calculatedAt;
}