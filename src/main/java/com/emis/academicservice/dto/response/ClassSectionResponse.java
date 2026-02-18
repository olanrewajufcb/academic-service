package com.emis.academicservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassSectionResponse {
    private Long sectionId;
    private SubjectResponse subject;
    private Long teacherId;
    private String teacherName;
    private String room;
    private String schedule;
    private Integer currentEnrollment;
    private Integer maxCapacity;
}