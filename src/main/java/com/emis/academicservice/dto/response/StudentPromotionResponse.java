package com.emis.academicservice.dto.response;

public record StudentPromotionResponse(
        String studentNumber,
        String schoolCode,
        String academicYear,
        Long classId,
        String className,
        String gradeLevel

) {
    public static StudentPromotionResponse from(StudentPromotionProjection projection) {
    return new StudentPromotionResponse(
       projection.getStudentNumber(),
       projection.getSchoolCode(),
       projection.getAcademicYear(),
       projection.getClassId(),
       projection.getClassName(),
       projection.getGradeLevel()
    );
    }
}
