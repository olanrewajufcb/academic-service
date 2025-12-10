package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.repository.StudentsInClassRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    SchoolClass toEntity(CreateSchoolClassRequest request);

    SchoolClassResponse toResponse(SchoolClass schoolClass);

    @Mapping(target = "name", source = "student.fullName")
    @Mapping(target = "schoolId", source = "student.schoolId")
    @Mapping(target = "classId", source = "row.classId")
    @Mapping(target = "className", source = "row.className")
    @Mapping(target = "classLevel", source = "row.classLevel")
    @Mapping(target = "formTeacherId", ignore = true)
    @Mapping(target = "arm", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "type", ignore = true)  // can set later if needed
    @Mapping(target = "level", ignore = true)
    StudentInClassResponse merge(StudentsInClassRow row, StudentDetailsResponse student);
}
