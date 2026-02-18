package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.enums.SchoolLevel;
import com.emis.academicservice.enums.SchoolType;
import com.emis.academicservice.repository.StudentsInClassRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "formTeacherName", ignore = true)

    @Mapping(target = "currentStudents", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    SchoolClass toEntity(CreateSchoolClassRequest request);

    SchoolClassResponse toResponse(SchoolClass schoolClass);


    @Mapping(target = "type", source = "schoolCode", qualifiedByName = "toSchoolType")
    @Mapping(target = "level", source = "gradeLevel", qualifiedByName = "toSchoolLevel")
    StudentInClassResponse responseFromRows(StudentsInClassRow row);

    @Named("toSchoolType")
    default SchoolType toSchoolType(String schoolCode) {
        if (schoolCode == null) return null;
        return switch (schoolCode.toUpperCase()) {
            case "PUBLIC" -> SchoolType.PUBLIC;
            case "PRIVATE" -> SchoolType.PRIVATE;
            default -> SchoolType.OTHER;
        };
    }

    @Named("toSchoolLevel")
    default SchoolLevel toSchoolLevel(String gradeLevel) {
        if (gradeLevel == null) return null;
        try {
            return SchoolLevel.valueOf(
                    gradeLevel.trim().toUpperCase().replace(" ", "_")
            );
        } catch (IllegalArgumentException e) {
            return SchoolLevel.PRIMARY;
        }
    }
}
