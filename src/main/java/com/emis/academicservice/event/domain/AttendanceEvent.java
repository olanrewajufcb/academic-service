package com.emis.academicservice.event.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceEvent {
    private Long attendanceId;
    private String studentNumber;
    private Long sectionId;
    private String schoolCode;
    private String attendanceStatus;
    private String notes;
    private LocalDate attendanceDate;
    private String correlationId;
}
