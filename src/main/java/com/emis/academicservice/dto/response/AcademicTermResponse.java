package com.emis.academicservice.dto.response;

import com.emis.academicservice.domain.db.AcademicTerm;

import java.time.LocalDate;

public record AcademicTermResponse(
        Long termId,
        LocalDate startDate,
        LocalDate endDate
) {
    public static AcademicTermResponse fromEntity(AcademicTerm academicTerm) {
        return new AcademicTermResponse(
                academicTerm.getTermId(),
                academicTerm.getStartDate(),
                academicTerm.getEndDate()
        );
    }
}
