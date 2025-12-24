package com.emis.academicservice.mapper;

import com.emis.academicservice.domain.db.Assessment;
import com.emis.academicservice.domain.db.MarkBookEntry;
import com.emis.academicservice.dto.response.MarkBookResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MarkBookMapper {
    
    @Mapping(target = "markEntryId", source = "markbookEntry.markEntryId")
    @Mapping(target = "assessmentId", source = "assessment.assessmentId")
    @Mapping(target = "assessmentName", source = "assessment.name")
    @Mapping(target = "studentId", source = "student.studentId")
    @Mapping(target = "studentName", expression = "java(student.firstName() + \" \" + student.lastName())")
    @Mapping(target = "scoreObtained", source = "markbookEntry.scoreObtained")
    @Mapping(target = "maxScore", source = "assessment.maxScore")
    @Mapping(target = "scorePercentage", source = "markbookEntry.scorePercentage")
    @Mapping(target = "remark", source = "markbookEntry.remark")
    @Mapping(target = "markedAt", source = "markbookEntry.markedAt")
    MarkBookResponse toMarkBookResponse(
        MarkBookEntry markbookEntry,
        Assessment assessment,
        StudentDetailsResponse student
    );
}