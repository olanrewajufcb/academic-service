package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.SchoolClassMapper;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.repository.StudentsInClassRow;
import com.emis.academicservice.service.ClassManagementService;
import com.emis.academicservice.service.client.HrClientService;
import com.emis.academicservice.service.client.SchoolClientService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassManagementServiceImp implements ClassManagementService {

    private final SchoolClassRepository schoolClassRepository;
    private  final SchoolClassMapper schoolClassMapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClientService schoolClientService;
    private final HrClientService hrClientService;
    private final SchoolCacheService schoolCacheService;
    private final StudentClientService studentClientService;


    @Override
    public Mono<SchoolClassResponse> createSchoolClass(CreateSchoolClassRequest request, String requestId) {
        SchoolClass schoolClass = schoolClassMapper.toEntity(request);
        schoolClass.setCurrentStudents(0);

        return schoolClientService.validateSchoolExists(request.getSchoolId())
                .flatMap(schoolExists -> {
                    if(Boolean.FALSE.equals(schoolExists)) {
                        return Mono.error(new SchoolNotFoundException(
                                "School with " + request.getSchoolId() + " does not exist"));
                    }
                    return Mono.just(true);
                })
                .flatMap(__ -> {
                    if(request.getFormTeacherId() == null) {
                        return Mono.just(true);
                    }
                    return validatedFormTeacher(request.getFormTeacherId())
                            .thenReturn(true);
                })
                .then(schoolClassRepository.existsBySchoolIdAndClassNameAndAcademicYear(
                        request.getSchoolId(),
                        request.getClassName(),
                        request.getAcademicYear()))
                .flatMap(duplicateExists -> {
                    if (Boolean.TRUE.equals(duplicateExists)) {
                        return Mono.error(new DuplicateClassException(
                                String.format("Class %s already exists for school %s in academic year %s",
                                        request.getClassName(), request.getSchoolId(), request.getAcademicYear())
                        ));
                    }
                    return Mono.just(false);
                })
                .then(Mono.defer(() ->  schoolClassRepository.save(schoolClass)))
                .as(transactionalOperator::transactional)
                .doOnSuccess(savedClass -> log.info("School class created successfully with id {} | requestId: {}",
                        savedClass.getClassId(), requestId))
                .map(schoolClassMapper::toResponse)
                .onErrorMap(DataIntegrityViolationException.class, ex ->
                        new DataIntegrityViolationException("Data integrity violation: " + ex.getMessage(), ex))
                .onErrorResume(ex -> {
                    log.error("Failed to create school class | requestId {} | error {}", requestId, ex.getMessage());
                    return Mono.error(new SchoolClassCreationException("Failed to create school class " + requestId, ex));
                });
    }

    private Mono<Void> validatedFormTeacher(Long teacherId) {
        return hrClientService.validateTeacherExists(teacherId)
                .flatMap(teacherExists -> {
                    if (Boolean.FALSE.equals(teacherExists)) {
                        return Mono.error(new TeacherNotFoundException(
                                "Teacher with ID " + teacherId + " not found or not assigned to school " ));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Flux<SchoolClassResponse> getSchoolClassBySchoolId(String schoolCode, String academicYear,
                                                              Pageable pageable, String requestId) {
        return schoolCacheService.getSchoolIdByCode(schoolCode)
                .switchIfEmpty(Mono.error(new SchoolClassNotFoundException("School not found with code " + schoolCode)))
                .flatMapMany(schoolId -> schoolClassRepository.findBySchoolIdAndAcademicYear(schoolId,
                        academicYear, pageable.getPageSize(),  pageable.getOffset())
                )
                .switchIfEmpty(Mono.error(new SchoolClassNotFoundException("No classes found for " + schoolCode +
                        "and the given academic year " + academicYear )))
                .map(schoolClassMapper::toResponse);
    }

    @Override
    public Flux<StudentInClassResponse> getStudentInClassByClassId(Long classId, Pageable pageable, String requestId) {
    return schoolClassRepository
        .getAllStudentsClass(classId)
        .collectList()
        .flatMapMany(
            rows -> {
              if (rows.isEmpty()) {
                return Flux.error(new SchoolClassNotFoundException("No classes found for " + classId));
              }
                List<Long> studentId = rows.stream()
                        .map(StudentsInClassRow::getStudentId)
                        .toList();
              return studentClientService.getStudentDetailsBatch(studentId)
                      .collectList()
                      .flatMapMany(students -> {
                          Map<Long, StudentDetailsResponse> lookup = students.stream()
                                  .collect(Collectors.toMap(StudentDetailsResponse::studentId, studt -> studt));
                          return  Flux.fromIterable(rows)
                                  .map(row -> schoolClassMapper.merge(row, lookup.get(row.getStudentId())));
                      });

            });

    }
}

