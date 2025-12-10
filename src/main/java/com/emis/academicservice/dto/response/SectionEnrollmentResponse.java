package com.emis.academicservice.dto.response;

import lombok.Data;

import java.time.LocalDate;
@Data
public class SectionEnrollmentResponse {
    private Long sectionId;
    private Long studentId;
    private LocalDate enrollmentDate;
}
