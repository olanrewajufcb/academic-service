package com.emis.academicservice.service.imp;

import com.emis.academicservice.dto.response.StudentClassesResponses;
import com.emis.academicservice.exception.StudentNotFoundException;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.repository.StudentClassesPerYear;
import com.emis.academicservice.service.StudentClassesService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StudentClassesServiceImp implements StudentClassesService {

    private final StudentClientService studentClientService;
    private final SchoolClassRepository repository;

    @Override
    public Flux<StudentClassesPerYear> getStudentsClasses(String studentNumber, String academicYear,
                                                          Pageable pageable, String requestId) {

        int size = pageable.getPageSize();
        long offset = pageable.getPageNumber();

        return studentClientService.getStudentDetails(studentNumber)
                .flatMapMany(student ->
                        Mono.zip(repository.getStudentClassesPerAcademicYear(academicYear,student.studentId(),
                                size, offset).collectList(),
                                repository.countStudentClasses(academicYear, student.studentId())
                                )
                                .flatMapMany(tuple -> {
                                    List<StudentClassesPerYear> studentClasses = tuple.getT1();
                                    long total = tuple.getT2();

                                    if (total == 0) {
                                        return Flux.empty();
                                    }
                                    return Flux.fromIterable(studentClasses);
                                }));

    }
}
