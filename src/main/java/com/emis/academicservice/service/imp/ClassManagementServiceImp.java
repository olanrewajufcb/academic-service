package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
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
public class ClassManagementServiceImp implements ClassManagementService {

    private final SchoolClassRepository schoolClassRepository;
    private  final SchoolClassMapper schoolClassMapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClientService schoolClientService;
    private final HrClientService hrClientService;
    private final SchoolCacheService schoolCacheService;


    @Override
    public Mono<SchoolClassResponse> createSchoolClass(CreateSchoolClassRequest request, String requestId) {


        return transactionalOperator.execute(status ->
                schoolClientService.validateSchoolExistsByCode(request.getSchoolCode()))
                .flatMap(schoolExists -> {
                    if (Boolean.FALSE.equals(schoolExists)) {
                        return Mono.error(new SchoolNotFoundException("School not found: " + request.getSchoolCode()));
                    }
                    return Mono.just(true);
                })
                .then(request.getFormTeacherId() != null
                        ? validatedFormTeacher(request.getFormTeacherId())
                        : Mono.empty())
                .then(Mono.defer(() -> {
                    SchoolClass schoolClass = schoolClassMapper.toEntity(request);
                    schoolClass.setCurrentStudents(0);
                    return schoolClassRepository.save(schoolClass);
                }))
        .doOnSuccess(
            savedClass ->
                log.info(
                    "School class created successfully with id {} | requestId: {}",
                    savedClass.getClassId(),
                    requestId))
        .map(schoolClassMapper::toResponse)
        .onErrorMap(
            DuplicateKeyException.class,
            e -> new DuplicateClassException("Class already exists " + requestId))
        .onErrorMap(
            DataIntegrityViolationException.class,
            ex ->
                new DataIntegrityViolationException(
                    "DB constraint failed : " + ex.getMessage(), ex))
        .onErrorResume(
            ex -> {
              log.error(
                  "Failed to create school class | requestId {} | error {}",
                  requestId,
                  ex.getMessage());
                if (ex instanceof SchoolNotFoundException || ex instanceof TeacherNotFoundException) {
                    return Mono.error(ex);
                }
              return Mono.error(
                  new SchoolClassCreationException(
                      "Failed to create school class " + requestId, ex));
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
    public Mono<Page<SchoolClassResponse>> getSchoolClassBySchoolId(String schoolCode, String academicYear,
                                                                   Pageable pageable, String requestId) {

        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();

        return schoolCacheService.getSchoolIdByCode(schoolCode)
                .switchIfEmpty(Mono.error(new SchoolClassNotFoundException("School not found with code " + schoolCode)))
                .flatMap(schoolId -> Mono.zip(
                        Mono.just(schoolId),
                        schoolClassRepository.findBySchoolIdAndAcademicYear(schoolId,
                        academicYear, pageSize,  offset).collectList(),
                        schoolClassRepository.countBySchoolIdAndAcademicYear(schoolId,academicYear))
                )
                .timeout(Duration.ofSeconds(5))
                .flatMap(tuple -> {
                    long schoolId = tuple.getT1();
                    List<SchoolClass> schoolClassList = tuple.getT2();
                   long total =  tuple.getT3();

                   if (total == 0) {
                       return Mono.error(new SchoolClassNotFoundException("No classes found for " +
                               schoolCode + " and the given academic year: " + academicYear));
                   }
                   var responses = schoolClassList.stream()
                           .map(schoolClassMapper::toResponse)
                           .toList();

                   Page<SchoolClassResponse> page = new PageImpl<>(responses, pageable, total);

                    log.info("[{}] Retrieved {} classes (page {}/{}) for schoolId: {} | schoolCode: {}",
                            requestId, page.getNumberOfElements(),
                            page.getNumber() + 1, page.getTotalPages(),
                            schoolId, schoolCode);

                   return Mono.just(page);

                })
                .doOnSuccess(page ->
                        log.info("[{}] Retrieved {} classes (page {}/{}) for schoolId: {}",
                                requestId, page.getNumberOfElements(),
                                page.getNumber() + 1, page.getTotalPages(), schoolCode))
                .onErrorMap(TimeoutException.class,
                        ex -> new SchoolClassFailureException("Request timeout after 5s", ex))
                .onErrorMap(error -> {
                    log.error("[{}] Failed to fetch classes for classId: {}",
                            requestId, schoolCode, error);
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

              if (total == 0) {
                  Page<StudentInClassResponse> emptyPage = Page.empty();
                  return Mono.just(emptyPage);
              }
             var responses = studentsInClassRows.stream()
                      .map(schoolClassMapper::responseFromRows)
                      .toList();

              Page<StudentInClassResponse> page = new PageImpl<>(responses, pageable, total);

                log.info("[{}] Retrieved {} students (page {}/{}) for classId: {}",
                        requestId, page.getNumberOfElements(),
                        page.getNumber() + 1, page.getTotalPages(),
                        classId);

                return  Mono.just(page);
            })

            .onErrorMap(TimeoutException.class,
                    ex -> new SchoolClassFailureException("Database timeout", ex))
            .onErrorMap(error -> {
                log.error("[{}] Failed to fetch students for classId: {}",
                        requestId, classId, error);
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

