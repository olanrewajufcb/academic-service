package com.emis.academicservice.dto.response;

public record AssessmentSummary(
    Integer totalAssessments,
    Integer examsCount,
    Integer testsCount,
    Integer assignmentsCount,
    Integer completedCount,
    Integer upcomingCount,
    Integer overdueCount
) {}