package com.emis.academicservice.dto.response;

import com.emis.academicservice.domain.db.Lesson;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonResponse(
        Long lessonId,
        Long sectionId,
        String teacherName

) {
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getSectionId(),
                lesson.getTeacherName()
        );
    }
}
