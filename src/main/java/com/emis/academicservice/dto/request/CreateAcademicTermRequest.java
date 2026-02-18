package com.emis.academicservice.dto.request;

import java.time.LocalDate;

public record CreateAcademicTermRequest(
    String termCode,
    String name,
    LocalDate startDate,
    LocalDate endDate
    ) {}
