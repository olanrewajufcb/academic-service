package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.StudentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse(Long enrollmentId,
                                 Long studentId,
                                 String studentName,
                                 String studentNumber,
                                 Long classId,
                                 String className,
                                 LocalDateTime enrollmentDate,
                                 StudentStatus status) {}
