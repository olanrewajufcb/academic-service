package com.emis.academicservice.domain.db;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Table(name = "lessons", schema = "academic_schema")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {

    @Id
    @Column("lesson_id")
    private Long lessonId;

    @Column("section_id")
    private Long sectionId;

    @Column("term_id")
    private Long termId;

    @Column("school_id")
    private Long schoolId;

    @Column("school_code")
    private String schoolCode;

    @Column("lesson_title")
    private String lessonTitle;

    @Column("topic")
    private String topic;

    @Column("description")
    private String description;

    @Column("lesson_date")
    private LocalDate lessonDate;

    @Column("start_time")
    private LocalTime startTime;

    @Column("end_time")
    private LocalTime endTime;

    @Column("teacher_id")
    private Long teacherId;

    @Column("teacher_name")
    private String teacherName;

    @Column("status")
    private String status;

    @Column("materials")
    private JsonNode materials; // JSONB stored as String

    @Column("homework_due_date")
    private LocalDate homeworkDueDate;

    @Column("homework_description")
    private String homeworkDescription;

    @Column("is_deleted")
    private Boolean isDeleted;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
