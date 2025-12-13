package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {


    @Mapping(target = "enrollmentId", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "enrollmentDate", ignore = true)
    @Mapping(target = "enrollmentStatus", ignore = true)
    @Mapping(target = "admittedBy", ignore = true)
    @Mapping(target = "admittedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    Enrollment toEntity(EnrollStudentRequest request);

    @Mapping(target = "className", ignore = true)
    @Mapping(target = "status", ignore = true)
    EnrollmentResponse toResponse(Enrollment enrollment);

}
