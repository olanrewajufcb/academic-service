package com.emis.academicservice.event.domain;

import lombok.Data;

@Data
public class StaffAssignedEvent {
    private Long assignmentId;
    private Long schoolId;
    private String schoolCode;
    private Long staffId;
    private String staffCode;
    private String staffName;
    private Long classId;
    private Long sectionId;
    private Long subjectId;
    private String academicYear;
    private String assignmentRole;
}