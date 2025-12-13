package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
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
    @Mapping(target = "schoolId", ignore = true)
    @Mapping(target = "formTeacherName", ignore = true)

    @Mapping(target = "currentStudents", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    SchoolClass toEntity(CreateSchoolClassRequest request);

    SchoolClassResponse toResponse(SchoolClass schoolClass);

//    @Mapping(target = "name", source = "student.fullName")
//    @Mapping(target = "schoolId", source = "student.schoolId")
//    @Mapping(target = "classId", source = "row.classId")
//    @Mapping(target = "className", source = "row.className")
//    @Mapping(target = "classLevel", source = "row.classLevel")
//    @Mapping(target = "formTeacherId", ignore = true)
//    @Mapping(target = "arm", ignore = true)
//    @Mapping(target = "academicYear", ignore = true)
//    @Mapping(target = "type", ignore = true)  // can set later if needed
//    @Mapping(target = "level", ignore = true)
//    StudentInClassResponse merge(StudentsInClassRow row, StudentDetailsResponse student);

    @Mapping(target = "type", source = "schoolCode", qualifiedByName = "toSchoolType")
    @Mapping(target = "level", source = "classLevel", qualifiedByName = "toSchoolLevel")
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
    default SchoolLevel toSchoolLevel(String classLevel) {
        if (classLevel == null) return null;
        try {
            return SchoolLevel.valueOf(
                    classLevel.trim().toUpperCase().replace(" ", "_")
            );
        } catch (IllegalArgumentException e) {
            return SchoolLevel.PRIMARY;
        }
    }
}
