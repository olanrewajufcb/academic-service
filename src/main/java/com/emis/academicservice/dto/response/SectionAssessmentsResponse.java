package com.emis.academicservice.dto.response;

import java.util.List;

public record SectionAssessmentsResponse(
    Long sectionId,
    String schoolCode,
    String sectionCode,
    String className,
    String subjectName,
    List<AssessmentResponse> assessments,
    AssessmentSummary summary){}