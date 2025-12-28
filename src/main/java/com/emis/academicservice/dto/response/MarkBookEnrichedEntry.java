package com.emis.academicservice.dto.response;

public record MarkBookEnrichedEntry(
    MarkBookEntryDetail entry,
    String studentNumber,
    String studentName,
    String gradeLevel
) {}