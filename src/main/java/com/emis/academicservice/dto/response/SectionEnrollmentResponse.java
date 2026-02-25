package com.emis.academicservice.dto.response;


import com.emis.academicservice.domain.db.SectionEnrollment;

import java.time.LocalDate;

public record SectionEnrollmentResponse(
        Long sectionId,
        Long studentId,
        LocalDate enrollmentDate
) {
    public static SectionEnrollmentResponse from(SectionEnrollment sectionEnrollment) {
        return new SectionEnrollmentResponse(
                sectionEnrollment.getSectionId(),
                sectionEnrollment.getStudentId(),
                sectionEnrollment.getEnrollmentDate()
        );
    }
}
