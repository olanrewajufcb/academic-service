package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.Lesson;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

public interface LessonRepository extends ReactiveCrudRepository<Lesson, Long> {

    Mono<Lesson> findBySectionIdAndLessonDateAndStartTime(Long sectionId,
                                                          LocalDate lessonDate, LocalTime startTime);
}
