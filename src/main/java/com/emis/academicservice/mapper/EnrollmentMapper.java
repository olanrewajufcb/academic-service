package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.Enrollment;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.response.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {


    @Mapping(target = "enrollmentId", ignore = true)
    Enrollment toEntity(EnrollStudentRequest request);

    EnrollmentResponse toResponse(Enrollment enrollment);

}
