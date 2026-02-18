package com.emis.academicservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateClassSectionRequest {
    private Long termId;
    private Long classId;
    private Long subjectId;
    private String room;
    private String schedule;
    private Integer maxCapacity;

}

