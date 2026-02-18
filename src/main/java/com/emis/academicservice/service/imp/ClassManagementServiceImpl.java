package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.domain.db.SchoolClass;
import com.emis.academicservice.dto.request.CreateSchoolClassRequest;
import com.emis.academicservice.dto.response.SchoolClassResponse;
import com.emis.academicservice.dto.response.StudentInClassResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.SchoolClassMapper;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.repository.StudentsInClassRow;
import com.emis.academicservice.service.ClassManagementService;
import com.emis.academicservice.service.client.HrClientService;
import com.emis.academicservice.service.client.SchoolClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassManagementServiceImpl implements ClassManagementService {

    private final SchoolClassRepository schoolClassRepository;
    private  final SchoolClassMapper schoolClassMapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClientService schoolClientService;
    private final HrClientService hrClientService;
    private final SchoolCacheService schoolCacheService;
    private final ServiceConfigurationProperties properties;


    @Override
    public Mono<SchoolClassResponse> createSchoolClass(CreateSchoolClassRequest request, String requestId) {

    return schoolClientService
        .validateSchoolExistsByCode(request.getSchoolCode())
        .flatMap(
            schoolExists -> {
              if (Boolean.FALSE.equals(schoolExists)) {
                return Mono.error(
                    new SchoolNotFoundException("School not found: " + request.getSchoolCode()));
              }
              return Mono.just(true);
            })
        .then(
            Mono.defer(
                () -> {
                  SchoolClass schoolClass = schoolClassMapper.toEntity(request);
                  schoolClass.setCurrentStudents(0);
                  return schoolClassRepository.save(schoolClass);
                }))
        .as(transactionalOperator::transactional)
        .doOnSuccess(
            savedClass ->
                log.info(
                    "School class created successfully with id {} | requestId: {}",
                    savedClass.getClassId(),
                    requestId))
        .map(schoolClassMapper::toResponse)
        .onErrorMap(
            ex -> {
              if (ex instanceof DuplicateKeyException) {
                log.error("Class already exists | requestId: {}", requestId);
                return new DuplicateClassException("Class already exists " + requestId);
              } else if (ex instanceof DataIntegrityViolationException) {
                log.error("DB constraint failed | requestId::::", ex);
                return new SchoolClassFailureException("DB constraint failed ", ex);
              } else if (ex instanceof SchoolNotFoundException) {
                log.error("School not found error occurred | requestId: {}", requestId);
                return new SchoolNotFoundException(ex.getMessage());
              }
              else if (ex instanceof SchoolServiceException) {
                log.error("School not found error occurred | requestId: {}", requestId);
                return new SchoolServiceException(
                    ex.getMessage() + request.getSchoolCode(), ex);
              }
              return new SchoolClassCreationException(
                  "Failed to create school class " + requestId, ex);
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
    public Mono<Page<SchoolClassResponse>> getSchoolClassBySchoolCode(String schoolCode, String academicYear,
                                                                   Pageable pageable, String requestId) {

        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();

        return Mono.zip(
                        schoolClassRepository.findBySchoolCodeAndAcademicYear(schoolCode,
                        academicYear, pageSize,  offset).collectList(),
                        schoolClassRepository.countBySchoolCodeAndAcademicYear(schoolCode,academicYear))
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .flatMap(tuple -> {
                    List<SchoolClass> schoolClassList = tuple.getT1();
                   long total =  tuple.getT2();

                   List<SchoolClassResponse> responses = total == 0
                           ? List.of()
                           : schoolClassList.stream()
                           .map(schoolClassMapper::toResponse)
                           .toList();

                   Page<SchoolClassResponse> page = new PageImpl<>(responses, pageable, total);

                    log.info("[{}] Retrieved {} classes (page {}/{}) for | schoolCode: {}",
                            requestId, page.getNumberOfElements(),
                            page.getNumber() + 1, page.getTotalPages(),
                             schoolCode);

                   return Mono.just(page);

                })
                .doOnSuccess(page ->
                        log.info("[{}] Retrieved {} classes (page {}/{}) for schoolId: {}",
                                requestId, page.getNumberOfElements(),
                                page.getNumber() + 1, page.getTotalPages(), schoolCode))

                .onErrorMap(error -> {
                    log.error("[{}] Failed to fetch classes for classId: {}",
                            requestId, schoolCode, error);
                    if (error instanceof TimeoutException){
                        return new DatabaseTimeoutException("Database operation timed out:::", error);
                    }
                    return new SchoolClassFailureException(
                            "Failed to fetch classes for schoolCode: " + schoolCode, error);
                });
    }

    @Override
    public Mono<Page<StudentInClassResponse>> getStudentInClassByClassId(Long classId, Pageable pageable, String requestId) {
        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();
        String sortColumn = getDbSortColumn(pageable.getSort());
    return Mono.zip(schoolClassRepository
        .getAllStudentsClass(classId, pageSize, offset, sortColumn).collectList(),
            schoolClassRepository.countActiveStudentsInClass(classId)
        )
            .timeout(Duration.ofSeconds(3))
            .flatMap(tuple -> {

             List<StudentsInClassRow> studentsInClassRows =   tuple.getT1();
              long total =  tuple.getT2();

             List<StudentInClassResponse> responses = total == 0
                     ? List.of()
                      : studentsInClassRows.stream()
                      .map(schoolClassMapper::responseFromRows)
                      .toList();

              Page<StudentInClassResponse> page = new PageImpl<>(responses, pageable, total);

                log.info("[{}] Retrieved {} students (page {}/{}) for classId: {}",
                        requestId, page.getNumberOfElements(),
                        page.getNumber() + 1, page.getTotalPages(),
                        classId);

                return Mono.just(page);
            })

            .onErrorMap(error -> {
                log.error("[{}] Failed to fetch students for classId: {}",
                        requestId, classId, error);
                if (error instanceof TimeoutException){
                    return new DatabaseTimeoutException("Database operation timed out", error);
                }
                return  new SchoolClassFailureException("Failed to fetch students", error);
            });
    }

  private String getDbSortColumn(Sort sort) {
    return sort.stream()
        .findFirst()
        .map(
            order ->
                switch (order.getProperty()) {
                  case "studentName" -> "student_name";
                  case "studentNumber" -> "student_number";
                  case "schoolName" -> "school_name";
                  default -> "student_name";
                })
        .orElse("student_name");
        }



}

