package com.emis.academicservice.service.imp;

import com.emis.academicservice.dto.response.StudentClassesResponses;
import com.emis.academicservice.exception.StudentNotFoundException;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.repository.StudentClassesPerYear;
import com.emis.academicservice.service.StudentClassesService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class StudentClassesServiceImp implements StudentClassesService {

    private final StudentClientService studentClientService;
    private final SchoolClassRepository repository;

    @Override
    public Flux<StudentClassesPerYear> getStudentsClasses(String studentNumber, String academicYear, String requestId) {
        return studentClientService.getStudentDetails(studentNumber)
                .flatMapMany(response -> {
                    Long studentId = response.studentId();
                    return repository.getStudentClassesPerAcademicYear(studentId, academicYear);
                })
                .switchIfEmpty(Mono.error(new StudentNotFoundException("No records for the given student " + studentNumber)));
    }
}
