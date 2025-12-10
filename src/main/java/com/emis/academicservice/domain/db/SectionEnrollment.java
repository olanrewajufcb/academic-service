package com.emis.academicservice.domain.db;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;


@Data
@Builder
@Table(name = "section_enrollments")
public class SectionEnrollment {
    @Id
    private Long enrollmentId;
    private Long sectionId;
    private Long studentId;
    private LocalDate enrollmentDate;
}
