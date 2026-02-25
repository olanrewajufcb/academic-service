package com.emis.academicservice.service;

import com.emis.academicservice.dto.request.CreateClassSectionRequest;
import com.emis.academicservice.dto.request.CreateLessonRequest;
import com.emis.academicservice.dto.request.StaffUpdateRequest;
import com.emis.academicservice.dto.response.ClassSectionResponse;
import com.emis.academicservice.dto.response.ClassSectionWithSubjectResponse;
import com.emis.academicservice.dto.response.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassSectionService {

    Mono<ClassSectionResponse> createClassSection(CreateClassSectionRequest request,
                                                 String schoolCode, String requestId);
    Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByClassId(
            Long classId,String schoolCode, Pageable pageable, String requestId);

    Mono<ClassSectionResponse> getClassSectionsByClassIdAndStaffCode(Long classId,
                   String schoolCode, String staffCode, String requestId);

    Mono<ClassSectionResponse> updateClassSection(
            Long classId, Long sectionId, String schoolCode,StaffUpdateRequest request,String requestId);

    Mono<LessonResponse> createLesson(String schoolCode, Long sectionId,
                                      CreateLessonRequest request, String requestId);

    Mono<Page<ClassSectionWithSubjectResponse>> getAllClassSectionsByStaffCode(String schoolCode,
                                                String staffCode, Pageable pageable, String requestId);

}
