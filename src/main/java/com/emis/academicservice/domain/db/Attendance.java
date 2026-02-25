package com.emis.academicservice.domain.db;

import com.emis.academicservice.enums.AttendanceStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@Table(name = "student_attendance")
public class Attendance {
    @Id
    private Long attendanceId;
    private Long sectionId;
    private Long studentId;
    private AttendanceStatus status;
    private String notes;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime recordedAt;
    private Long recordedBy;
    private LocalDateTime createdAt;
}