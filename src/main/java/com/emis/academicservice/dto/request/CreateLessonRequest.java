package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.LessonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateLessonRequest(
        @NotNull(message = "Term Id is required")
        Long termId,
        @NotBlank(message = "Lesson Title is required")
        String lessonTitle,
        String topic,
        String description,
        @NotNull(message = "Lesson Date is required")
        LocalDate lessonDate,
        @NotNull(message = "Lesson Start Time is required")
        LocalTime startTime,
        @NotNull(message = "Lesson End Time is required")
        LocalTime endTime,
//        Long teacherId,
        String teacherName,
        LessonStatus status,
        LessonMaterials materials,
        LocalDate homeworkDueDate,
        String homeworkDescription

) {}
