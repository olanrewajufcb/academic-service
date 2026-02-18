package com.emis.academicservice.dto.response;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.enums.EnrollmentStatus;

import java.time.LocalDateTime;

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
}
