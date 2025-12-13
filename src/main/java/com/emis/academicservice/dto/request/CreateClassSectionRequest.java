package com.emis.academicservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateClassSectionRequest {
    private String schoolCode;
    private Long classId;
    private Long subjectId;
    private Long teacherId;
    private String room;
    private String schedule;
    private Integer maxCapacity;

}

