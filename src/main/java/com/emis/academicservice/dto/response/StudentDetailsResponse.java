package com.emis.academicservice.dto.response;

import com.emis.academicservice.enums.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentDetailsResponse(Long studentId,
                                     Long schoolId,
                                     String studentNumber,
                                     String firstName,
                                     String lastName,
                                     String fullName,
                                     String schoolCode,
                                     LocalDate dateOfBirth,
                                     String gender,
                                     LocalDateTime enrollmentDate,
                                     String classLevel,
                                     StudentStatus status,
                                     String email,
                                     String phone,
                                     String address1,
                                     String address2,
                                     String ward,
                                     String city,
                                     String lga,
                                     String state) {}
