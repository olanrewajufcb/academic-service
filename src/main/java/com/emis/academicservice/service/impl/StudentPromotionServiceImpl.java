package com.emis.academicservice.service.impl;

import com.emis.academicservice.dto.request.EnrollStudentInClassSectionRequest;
import com.emis.academicservice.dto.request.EnrollStudentRequest;
import com.emis.academicservice.dto.request.StudentPromotionRequest;
import com.emis.academicservice.dto.response.StudentPromotionResponse;
import com.emis.academicservice.enums.GradeLevel;
import com.emis.academicservice.exception.ValidationException;
import com.emis.academicservice.repository.ClassSectionRepository;
import com.emis.academicservice.repository.SchoolClassProjection;
import com.emis.academicservice.repository.SchoolClassRepository;
import com.emis.academicservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.emis.academicservice.enums.GradeLevel.GRADUATE;

@Slf4j
@RequiredArgsConstructor
@Service
public class StudentPromotionServiceImpl implements StudentPromotionService {
    private final ClassSectionRepository  classSectionRepository;
    private final ActiveStudentEnrollmentCache studentEnrollmentCache;
    private final SchoolClassRepository schoolClassRepository;
    private final EnrollmentService enrollmentService;
    private final SectionEnrollmentService sectionEnrollmentService;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<StudentPromotionResponse> promoteStudent(
            String studentNumber,
            String idempotencyKey,
            StudentPromotionRequest request,
            String requestId) {

        return studentEnrollmentCache
                .getStudentEnrollmentFromCache(
                        request.schoolCode(),
                        studentNumber,
                        request.academicYear()
                )

                .flatMap(validation ->
                        schoolClassRepository
                                .findClassBySchoolCodeAndId(
                                        request.schoolCode(),
                                        request.currentClassId()
                                )


                .flatMap(currentClass -> {

                    GradeLevel current =
                            GradeLevel.valueOf(currentClass.getGradeLevel());

                    GradeLevel expectedNext = getNextGradeLevel(current);

                    if (expectedNext == GRADUATE) {
                        return Mono.error(
                                new ValidationException(
                                        "Student cannot be promoted beyond SSS_THREE"));
                    }

                    return schoolClassRepository
                            .findClassBySchoolCodeAndId(
                                    request.schoolCode(),
                                    request.nextClassId()
                            )

                            .flatMap(nextClass -> {

                                if (!nextClass.getGradeLevel()
                                        .equals(expectedNext.name())) {
                                    return Mono.error(
                                            new ValidationException(
                                                    "Invalid next class for promotion"));
                                }

                                return performPromotion(
                                        studentNumber,
                                        request,
                                        currentClass,
                                        nextClass,
                                        idempotencyKey,
                                        validation.studentId(),
                                        requestId

                                );
                            });
                }))

                .as(transactionalOperator::transactional);
    }

    private GradeLevel getNextGradeLevel(GradeLevel gradeLevel) {
        return switch (GradeLevel.valueOf(gradeLevel.name())) {

            case PRIMARY_ONE -> GradeLevel.PRIMARY_TWO;
            case PRIMARY_TWO -> GradeLevel.PRIMARY_THREE;
            case PRIMARY_THREE -> GradeLevel.PRIMARY_FOUR;
            case PRIMARY_FOUR -> GradeLevel.PRIMARY_FIVE;
            case PRIMARY_FIVE -> GradeLevel.PRIMARY_SIX;
            case PRIMARY_SIX -> GradeLevel.JSS_ONE;
            case JSS_ONE -> GradeLevel.JSS_TWO;
            case JSS_TWO -> GradeLevel.JSS_THREE;
            case JSS_THREE -> GradeLevel.SS_ONE;
            case SS_ONE -> GradeLevel.SS_TWO;
            case SS_TWO -> GradeLevel.SS_THREE;
            case SS_THREE -> GRADUATE;
            default -> throw new IllegalArgumentException("Invalid grade level");
        };
    }

    private Mono<StudentPromotionResponse> performPromotion(
            String studentNumber,
            StudentPromotionRequest request,
            SchoolClassProjection currentClass,
            SchoolClassProjection nextClass,
            String idempotencyKey,
            Long studentId,
            String requestId) {

        EnrollStudentRequest enrollRequest =
                new EnrollStudentRequest(
                        request.schoolCode(),
                        studentNumber,
                        nextClass.getClassId()
                );

        return enrollmentService
                .removeStudentFromClass(
                        currentClass.getClassId(),
                        studentNumber,
                        request.schoolCode()
                )

                .thenMany(
                        classSectionRepository
                                .findAllByClassIdAndSchoolCode(
                                        currentClass.getClassId(),
                                        request.schoolCode()
                                )
                                .flatMap(section ->
                                        sectionEnrollmentService
                                                .removeStudentFromSection(
                                                        section.getSectionId(),
                                                        studentId

                                                )
                                )
                )
                .then()

                .then(enrollmentService.placeStudentInClass(
                        enrollRequest,
                        idempotencyKey
                ))

                .flatMapMany(enrollmentResponse ->
                        classSectionRepository
                                .findAllByClassIdAndSchoolCode(
                                        nextClass.getClassId(),
                                        request.schoolCode()
                                )
                                .flatMap(section ->
                                        sectionEnrollmentService
                                                .enrollStudentInClassSection(
                                                        section.getSectionId(),
                                                        new EnrollStudentInClassSectionRequest(
                                                                studentNumber,
                                                                request.schoolCode()
                                                        ),
                                                        requestId
                                                )
                                )
                )
                .then(Mono.just(
                        new StudentPromotionResponse(
                                studentNumber,
                                request.schoolCode(),
                                request.academicYear(),
                                nextClass.getClassId(),
                                nextClass.getClassName(),
                                nextClass.getGradeLevel()
                        )
                ));
    }
}
