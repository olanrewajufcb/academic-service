package com.emis.academicservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAttendanceSummaryProjection {
    Long totalLessons;
    Long present;
    Long absent;
    Long late;
}
