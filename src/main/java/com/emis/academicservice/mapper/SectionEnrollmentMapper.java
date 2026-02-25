package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.SectionEnrollment;
import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.response.SectionEnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionEnrollmentMapper {


    @Mapping(target = "sectionEnrollmentId", ignore = true)
    @Mapping(target = "sectionId", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "enrollmentDate", ignore = true)
    SectionEnrollment toEntity(EnrollStudentInClassSectionRequest request);


}
