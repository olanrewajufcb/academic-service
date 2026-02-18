package com.emis.academicservice.domain.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student_attendance", schema = "academic_schema")
public class StudentAttendance {
    @Id
    private Long attendanceId;
    private Long sectionId;
    private Long studentId;
    private String studentNumber;
    private String schoolCode;
    private LocalDate attendanceDate;
    private String attendanceStatus;
    private String notes;
    private LocalDate recordedAt;
    private Long recordedBy;
    private Boolean isDeleted;
    private LocalDate deletedAt;
    private LocalDate createdAt;
}
