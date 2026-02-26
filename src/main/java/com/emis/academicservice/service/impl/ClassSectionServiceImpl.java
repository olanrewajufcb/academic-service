package com.emis.academicservice.service.impl;

import com.emis.academicservice.domain.db.ClassSection;
import com.emis.academicservice.domain.db.Lesson;
import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.request.CreateLessonRequest;
import com.emis.academicservice.dto.request.StaffUpdateRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.ClassSectionWithSubjectResponse;
import com.emis.academicservice.dto.response.LessonResponse;
import com.emis.academicservice.exception.*;
import com.emis.academicservice.mapper.ClassSectionMapper;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.repository.LessonRepository;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.repository.SubjectWithSectionProjection;
import com.emis.academicservice.service.ClassSectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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


@RequiredArgsConstructor
@Slf4j
@Service
public class ClassSectionServiceImpl implements ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
    private final ClassSectionMapper mapper;
    private final TransactionalOperator transactionalOperator;
    private final SchoolClassRepository schoolClassRepository;
    private final LessonRepository lessonRepository;
    private final ObjectMapper objectMapper;
    @Override
    public Mono<ClassSectionResponse> createClassSection(CreateClassSectionRequest request,
                                                        String schoolCode, String requestId) {
        ClassSection section = mapper.toEntity(request);

    return schoolClassRepository
        .findClassBySchoolCodeAndId(schoolCode, request.getClassId())
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Class not found")))
        .flatMap(
            schoolClassProjection -> {
              section.setSchoolId(schoolClassProjection.getSchoolId());
              section.setSchoolCode(schoolCode);
              log.info("Logging section {}", section);
              return classSectionRepository.save(section);
            })
        .as(transactionalOperator::transactional)
        .doOnSuccess(
            savedClass ->
                log.info(
                    "Successfully created class section with id {} | requestId: {}",
                    savedClass,
                    requestId))
        .map(mapper::toResponse)
        .onErrorMap(
            ex -> {
              if (ex instanceof DuplicateKeyException) {
                return new ResourceNotFoundException(
                    "Class section already exists");
              } else if (ex instanceof DataIntegrityViolationException) {
                if (ex.getMessage().contains("violates foreign key constraint")) {
                  return new ValidationException("Wrong id supplied or missing id");
                }
                return new ClassSectionCreationException("Failed to create class section", ex);
              }
              log.error(
                  "Failed to create class section | requestId {} | error {}",
                  requestId,
                  ex.getMessage());
              return new ClassSectionCreationException(
                  "Failed to create class section " + requestId, ex);
            });
    }

    @Override
    public Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByClassId(Long classId,
                                                  String schoolCode, Pageable pageable, String requestId) {
        if (classId == null || classId <= 0) {
            return Mono.error(new InvalidParameterException("classId must be positive"));
        }
        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();
        String sortBy = pageable.getSort().stream().findFirst()
                .map(Sort.Order::getProperty)
                .orElse("sectionId");
    return schoolClassRepository
        .findClassBySchoolCodeAndId(schoolCode, classId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Class not found")))
        .flatMap(
            schoolClassProjection ->
                Mono.zip(
                    classSectionRepository
                        .findPageByClassIdThenJoinWithSubject(classId, sortBy, pageSize, offset)
                        .collectList(),
                    classSectionRepository.countByClassId(classId)))
        .timeout(Duration.ofSeconds(5))
        .map(
            tuple -> {
              List<SubjectWithSectionProjection> sections = tuple.getT1();
              long total = tuple.getT2();
              List<ClassSectionWithSubjectResponse> responses =
                  total == 0
                      ? List.of()
                      : sections.stream().map(ClassSectionWithSubjectResponse::from).toList();
              return (Page<ClassSectionWithSubjectResponse>)
                  new PageImpl<>(responses, pageable, total);
            })
        .doOnSuccess(
            page ->
                log.info(
                    "[{}] Retrieved {} class sections (page {}/{}) for classId: {}",
                    requestId,
                    page.getNumberOfElements(),
                    page.getNumber() + 1,
                    page.getTotalPages(),
                    classId))
        .onErrorMap(
            error -> {
              log.error(
                  "[{}] Failed to fetch class sections for classId: {}", requestId, classId, error);
              if (error instanceof TimeoutException) {
                return new DatabaseTimeoutException(
                    "Timeout while fetching class sections for classId:::: ", error);
              }
              return new AcademicServiceFailureException(
                  "Failed to fetch class sections for classId: " + classId);
            });
    }

    @Override
    public Mono<ClassSectionResponse> getClassSectionsByClassIdAndStaffCode(
            Long classId, String schoolCode, String staffCode, String requestId) {
    return schoolClassRepository
        .findClassBySchoolCodeAndId(schoolCode, classId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Class not found")))
        .flatMap(
            classProjection ->
                classSectionRepository.findByClassIdAndSectionIdAndTeacherCode(
                    classProjection.getClassId(), staffCode))
        .map(
            classSection ->
                ClassSectionResponse.builder()
                    .sectionId(classSection.getSectionId())
                    .teacherId(classSection.getTeacherId())
                    .room(classSection.getRoom())
                    .schedule(classSection.getSchedule())
                    .maxCapacity(classSection.getMaxCapacity())
                    .currentEnrollment(classSection.getCurrentEnrollment())
                    .build());
    }

    @Override
    public Mono<ClassSectionResponse> updateClassSection(Long classId, Long sectionId, String schoolCode,
                                                         StaffUpdateRequest request, String requestId) {

    return schoolClassRepository
        .findClassBySchoolCodeAndId(schoolCode, classId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Class not found")))
        .flatMap(
            classProjection ->
                classSectionRepository
                    .findById(sectionId)
                    .switchIfEmpty(
                        Mono.error(new ResourceNotFoundException("Class section not found")))
                    .flatMap(
                        classSection -> {
                          classSection.setTeacherId(request.staffId());
                          classSection.setStaffCode(request.staffCode());
                          classSection.setTeacherName(request.name());
                          return classSectionRepository.save(classSection);
                        })
                    .as(transactionalOperator::transactional))
        .map(
            classSection ->
                ClassSectionResponse.builder()
                    .sectionId(classSection.getSectionId())
                    .teacherId(classSection.getTeacherId())
                    .room(classSection.getRoom())
                    .schedule(classSection.getSchedule())
                    .maxCapacity(classSection.getMaxCapacity())
                    .currentEnrollment(classSection.getCurrentEnrollment())
                    .build());
    }


    public Mono<LessonResponse> createLesson(
            String schoolCode,
            Long sectionId,
            CreateLessonRequest request,
            String requestId) {

    // TODO: validate lesson_date inside term start_date/ end_date
    //  TODO: Possibly validate teacherId matches section.teacherId
    //  TODO:	Enforce time overlap prevention

    return classSectionRepository
        .findBySectionIdAndSchoolCode(sectionId, schoolCode)
        .switchIfEmpty(
            Mono.error(
                new ResourceNotFoundException("Section not found for school: " + schoolCode)))
        .flatMap(
            section -> {
              if (!section.getTermId().equals(request.termId())) {
                return Mono.error(new ValidationException("Lesson term must match section term"));
              }

              Lesson lesson = createLesson(request, section);

              return lessonRepository.save(lesson);
            })
        .map(LessonResponse::from)
        .doOnSuccess(
            r ->
                log.info(
                    "[{}] Lesson created for sectionId {} on {}",
                    requestId,
                    sectionId,
                    request.lessonDate()))
        .onErrorMap(
                DataIntegrityViolationException.class,
            ex -> {

                String msg = ex.getMessage();

                if (msg == null) {
                    return new ResourceCreationException("Lesson creation failed", ex);
                }

                if (msg.contains("no_teacher_double_booking")) {
                    return new ResourceAlreadyExistsException(
                            "Teacher is already assigned to another lesson at this time");
                }

                if (msg.contains("no_overlapping_lessons")) {
                    return new ResourceAlreadyExistsException(
                            "Section already has a lesson scheduled at this time");
                }

                if (msg.contains("uk_active_section")) {
                    return new ResourceAlreadyExistsException(
                            "Lesson already exists for this section and time");
                }

                return new ResourceCreationException("Lesson creation failed", ex);
    });

}

    private Lesson createLesson(CreateLessonRequest request, ClassSection section) {

    return Lesson.builder()
        .sectionId(section.getSectionId())
        .schoolId(section.getSchoolId())
        .schoolCode(section.getSchoolCode())
        .termId(request.termId())
        .lessonTitle(request.lessonTitle())
        .topic(request.topic())
        .description(request.description())
        .lessonDate(request.lessonDate())
        .startTime(request.startTime())
        .endTime(request.endTime())
        .teacherId(section.getTeacherId())
        .materials(objectMapper.valueToTree(request.materials()))
        .homeworkDueDate(request.homeworkDueDate())
        .homeworkDescription(request.homeworkDescription())
        .build();
    }


    @Override
    public Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByStaffCode(
            String schoolCode, String staffCode, Pageable pageable, String requestId) {

        int size = pageable.getPageSize();
        long offset = pageable.getOffset();
    return Mono.zip(classSectionRepository
                    .findBySchoolCodeAndTeacherCode(schoolCode, staffCode, size, offset)
                              .collectList(),
                classSectionRepository.countBySchoolCodeAndTeacherCode(schoolCode,  staffCode))
                .timeout(Duration.ofSeconds(5))
                .map(
                        tuple -> {
                            List<SubjectWithSectionProjection> sections = tuple.getT1();
                            long total = tuple.getT2();
                            List<ClassSectionWithSubjectResponse> responses = total == 0
                                    ? List.of()
                                    : sections.stream()
                                    .map(ClassSectionWithSubjectResponse::from)
                                    .toList();
                            return (Page<ClassSectionWithSubjectResponse>)  new PageImpl<>(responses, pageable, total);
                        })
                .doOnSuccess(
                        page ->
                                log.info(
                                        "[{}] Retrieved {} class sections (page {}/{}) for school code: {}",
                                        requestId,
                                        page.getNumberOfElements(),
                                        page.getNumber() + 1,
                                        page.getTotalPages(),
                                        schoolCode))
                .onErrorMap(
                        error -> {
                            log.error(
                                    "[{}] Failed to fetch class sections for school: {}", requestId, schoolCode, error);
                            if (error instanceof TimeoutException) {
                                return new DatabaseTimeoutException(
                                        "Timeout while fetching class sections for classId:::: ", error);
                            }
                            return new ClassSectionFailureException(
                                    "Failed to fetch class sections for school code: " + schoolCode, error);
                        });
    }
}
