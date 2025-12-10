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
    private String academicYear;
    private String enrollmentStatus; // e.g., "ENROLLED", "WITHDRAWN", "COMPLETED"
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
