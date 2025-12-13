package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.request.RegisterSubjectRequest;
import com.emis.academicservice.dto.response.SubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "schoolId", ignore = true)
    @Mapping(target = "schoolValidated", ignore = true)
    @Mapping(target = "lastSchoolValidation", ignore = true)

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Subject toEntity(RegisterSubjectRequest request);

    SubjectResponse toResponse(Subject subject);
}
