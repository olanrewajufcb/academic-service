package com.emis.academicservice.dto.response;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.enums.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrollmentResponse(Long enrollmentId,
                                 Long studentId,
                                 String studentName,
                                 String studentNumber,
                                 Long classId,
                                 String className,
                                 LocalDateTime enrollmentDate,
                                 EnrollmentStatus status) {
    public static EnrollmentResponse from(Enrollment enrollment,String studentName, String className) {
        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getStudentId(),
                studentName,
                enrollment.getStudentNumber(),
                enrollment.getClassId(),
                className,
                enrollment.getEnrollmentDate(),
                enrollment.getEnrollmentStatus());
    }

    public static EnrollmentResponse from(String msg) {
        return new EnrollmentResponse(
                null,
                null,
                msg,
                null,
                null,
                null,
                null,
                null

        );
    }
}
