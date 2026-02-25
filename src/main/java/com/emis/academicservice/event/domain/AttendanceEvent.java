package com.emis.academicservice.event.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceEvent {
    private Long attendanceId;
    private Long termId;
    private Long sectionId;
    private String studentNumber;
    private Long lessonId;
    private String schoolCode;
    private String attendanceStatus;
    private String notes;
    private LocalDate lessonDate;
    private UUID correlationId;
}
