-- =============================================
-- Academic Service Seed Data (2024/2025)
-- Soft Delete Version, Schema-qualified
-- =============================================

-- 1. Academic Term
INSERT INTO academic_schema.academic_term (school_id, term_code, name, start_date, end_date, is_current)
VALUES
    (1, '2024-FIRST-TERM', 'First Term 2024', '2024-09-01', '2024-12-15', TRUE)
    RETURNING term_id;

INSERT INTO academic_schema.academic_term (school_id, term_code, name, start_date, end_date, is_current)
VALUES
    (1, '2024-SECOND-TERM', 'Second Term 2024', '2025-01-08', '2025-03-28', FALSE)
    RETURNING term_id;

-- 2. Subjects (JSS Level)
INSERT INTO academic_schema.subjects (school_id, subject_code, name, grade_level, stage, status)
VALUES
    (1, 'MTH-JSS1', 'Mathematics', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE')
    RETURNING subject_id;

INSERT INTO academic_schema.subjects (school_id, subject_code, name, grade_level, stage, status)
VALUES
    (1, 'ENG-JSS1', 'English Language', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE')
    RETURNING subject_id;

INSERT INTO academic_schema.subjects (school_id, subject_code, name, grade_level, stage, status)
VALUES
    (1, 'BAS-SCI-JSS1', 'Basic Science', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE')
    RETURNING subject_id;

-- 3. School Classes
INSERT INTO academic_schema.school_classes (school_id, class_name, grade_level, arm, stage, academic_year, form_teacher_id)
VALUES
    (1, 'JSS 1A', 'JSS_1', 'A', 'JUNIOR_SECONDARY', '2024/2025', 101)
    RETURNING class_id;

INSERT INTO academic_schema.school_classes (school_id, class_name, grade_level, arm, stage, academic_year, form_teacher_id)
VALUES
    (1, 'JSS 1B', 'JSS_1', 'B', 'JUNIOR_SECONDARY', '2024/2025', 102)
    RETURNING class_id;

-- 4. Class Sections
-- Replace class_id and subject_id with actual returned IDs from above
-- Example using placeholders: <CLASS_ID>, <SUBJECT_ID>
INSERT INTO academic_schema.class_sections (school_id, class_id, subject_id, teacher_id, room, schedule, max_capacity)
VALUES
    (1, 1, 1, 101, 'Room 101', 'Monday 8:00-9:00, Wednesday 10:00-11:00', 45)
    RETURNING section_id;

INSERT INTO academic_schema.class_sections (school_id, class_id, subject_id, teacher_id, room, schedule, max_capacity)
VALUES
    (1, 1, 2, 103, 'Room 102', 'Tuesday 8:00-9:00, Thursday 10:00-11:00', 45)
    RETURNING section_id;

-- 5. Enrollments
INSERT INTO academic_schema.enrollments (student_id, class_id, enrollment_status)
VALUES
    (1001, 1, 'ENROLLED')
    RETURNING enrollment_id;

-- 6. Section Enrollments
INSERT INTO academic_schema.section_enrollments (section_id, student_id)
VALUES
    (1, 1001);

-- 7. Attendance
INSERT INTO academic_schema.attendance (section_id, student_id, attendance_date, attendance_status, recorded_by)
VALUES
    (1, 1001, '2024-09-02', 'PRESENT', 101),
    (1, 1001, '2024-09-04', 'ABSENT', 101);

-- 8. Assessments
INSERT INTO academic_schema.assessments (section_id, term_id, name, assessment_type, max_score, weight, due_date)
VALUES
    (1, 1, 'First Term Midterm Test', 'TEST', 30.00, 0.30, '2024-10-15')
    RETURNING assessment_id;

-- 9. Markbook Entry
INSERT INTO academic_schema.markbook_entry (assessment_id, student_id, term_id, score_obtained, marked_by)
VALUES
    (1, 1001, 1, 25.00, 101);
