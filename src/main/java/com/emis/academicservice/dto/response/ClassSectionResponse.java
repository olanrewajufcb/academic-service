package com.emis.academicservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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