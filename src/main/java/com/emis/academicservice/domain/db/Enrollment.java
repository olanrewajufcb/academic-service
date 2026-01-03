package com.emis.academicservice.domain.db;

import com.emis.academicservice.enums.EnrollmentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(name = "enrollments")
public class Enrollment {
    @Id
    private Long enrollmentId;
    private Long classId;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String academicYear;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus enrollmentStatus;
    private Long admittedBy;
    private LocalDateTime admittedAt;
    private String rejectionReason;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String idempotencyKey;

}
