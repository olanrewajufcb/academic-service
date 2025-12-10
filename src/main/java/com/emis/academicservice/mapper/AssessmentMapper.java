package com.emis.academicservice.mapper;


import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.dto.request.CreateAssessmentRequest;
import com.emis.academicservice.dto.response.AssessmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    @Mapping(target = "assessmentId", ignore = true)
    Assessment toEntity(CreateAssessmentRequest request);

    AssessmentResponse toResponse(Assessment entity);
}
