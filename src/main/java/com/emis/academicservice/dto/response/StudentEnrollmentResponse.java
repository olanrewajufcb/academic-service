package com.emis.academicservice.dto.response;

import java.time.LocalDate;

public record StudentEnrollmentResponse(
        Long enrollmentId,
        String studentNumber,
        Long studentId,
        String schoolCode,
        String enrollmentType,
        String studentName,
        String enrollmentStatus,
        LocalDate enrollmentDate
) {}