package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassSectionMapper {

    @Mapping(target = "sectionId", ignore = true)
    @Mapping(target = "schoolId", ignore = true)
    @Mapping(target = "currentEnrollment", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    ClassSection toEntity(CreateClassSectionRequest request);

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    ClassSectionResponse toResponse(ClassSection classSection);
}
