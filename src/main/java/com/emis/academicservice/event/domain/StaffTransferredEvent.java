package com.emis.academicservice.event.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StaffTransferredEvent {

    private Long historyId;

    private Long staffId;
    private String staffCode;

    private Long fromSchoolId;
    private String fromSchoolCode;

    private Long toSchoolId;
    private String toSchoolCode;

    private String newPosition;
    private LocalDate startDate;

    private String changeType; // TRANSFER
}