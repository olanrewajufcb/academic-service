package com.emis.academicservice.repository;

import com.emis.academicservice.domain.db.StudentAttendance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface StudentAttendanceRepository extends ReactiveCrudRepository<StudentAttendance, Long> {

    Mono<StudentAttendance> findBySectionIdAndStudentNumberAndAttendanceDate(
            Long sectionId, String studentNumber, LocalDate attendanceDate);

}
