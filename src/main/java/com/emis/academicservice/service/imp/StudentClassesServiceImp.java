package com.emis.academicservice.service.imp;

import com.emis.academicservice.cache.SchoolCacheService;
import com.emis.academicservice.domain.db.Subject;
import com.emis.academicservice.dto.response.StudentClassesResponses;
import com.emis.academicservice.dto.response.StudentMarksResponse;
import com.emis.academicservice.dto.response.SubjectName;
import com.emis.academicservice.exception.ClassSectionFailureException;
import com.emis.academicservice.exception.DatabaseTimeoutException;
import com.emis.academicservice.exception.StudentNotFoundException;
import com.emis.academicservice.repository.*;
import com.emis.academicservice.service.StudentClassesService;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Service
public class StudentClassesServiceImp implements StudentClassesService {

    private final StudentClientService studentClientService;
    private final SchoolClassRepository repository;
    private final AcademicTermRepository academicTermRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public Flux<StudentClassesPerYear> getStudentsClasses(String studentNumber,
                                                          String schoolCode,
                                                          String academicYear,
                                                          Pageable pageable, String requestId) {

        int size = pageable.getPageSize();
        long offset = pageable.getPageNumber();

        return studentClientService.getStudentDetails(studentNumber, schoolCode )
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

  @Override
  public Mono<Page<StudentMarksResponse>> getStudentMarks(
      String studentNumber,
      String schoolCode,
      String academicYear,
      Pageable pageable,
      String requestId) {

    int size = pageable.getPageSize();
    long offset = pageable.getPageNumber();

    return studentClientService
        .getStudentDetails(studentNumber, schoolCode)
        .flatMap(
            student ->
                Mono.zip(
                    academicTermRepository
                        .getStudentMarks(
                            student.studentId(), student.schoolId(), academicYear, size, offset)
                        .collectList(),
                    academicTermRepository.countStudentMarks(academicYear, student.studentId()),
                    enrichMarksWithSubjectDetails(student.studentId(), academicYear)))
        .timeout(Duration.ofSeconds(5))
        .flatMap(
            tuple -> {
              List<StudentMarksResponse> marks = tuple.getT1();
              long total = tuple.getT2();
              Map<Long, String> subjectNames = tuple.getT3();


              List<StudentMarksResponse> enrichMarks = total == 0
                  ? List.of()
                  : marks.stream()
                      .map(
                          mark ->
                              new StudentMarksResponse(
                                  mark.termId(),
                                  mark.startDate(),
                                  mark.endDate(),
                                  mark.studentId(),
                                  mark.sectionId(),
                                  mark.subjectId(),
                                  mark.totalScore(),
                                  mark.averageScore(),
                                  mark.positionInClass(),
                                  mark.remarks(),
                                  subjectNames.getOrDefault(mark.subjectId(), "Unknown Subject"),
                                  null))
                      .toList();
             return Mono.just((Page<StudentMarksResponse>) new PageImpl<>(enrichMarks, pageable, total));

            })
        .doOnSuccess(
            page ->
                log.info(
                    "[{}] Retrieved {} student marks (page {}/{}) for studentNumber: {}",
                    requestId,
                    page.getNumberOfElements(),
                    page.getNumber() + 1,
                    page.getTotalPages(),
                    studentNumber))
        .onErrorMap(
            ex -> {
              log.error(
                  "[{}] Failed to fetch student marks for studentNumber: {}",
                  requestId,
                  studentNumber,
                  ex);
              if (ex instanceof TimeoutException){
                  return new DatabaseTimeoutException("Database operation timed out:::", ex);
              }
              return new ClassSectionFailureException(
                  "Failed to fetch student marks for studentNumber: " + studentNumber, ex);
            });
        }

  private Mono<Map<Long, String>> enrichMarksWithSubjectDetails(
      Long studentId, String academicYear) {
        return academicTermRepository.getStudentSubjectIds(studentId, academicYear)
                .collectList()
            .flatMap(subjectIds -> {
                if (subjectIds.isEmpty()){
                    return Mono.just(Map.of());
                }
                return subjectRepository.findNamesByIds(subjectIds)
                        .collectMap(SubjectName::subjectId, SubjectName::name);
            });
        }

}
