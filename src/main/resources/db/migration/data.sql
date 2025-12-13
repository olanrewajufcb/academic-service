-- Academic term (2024/2025)
INSERT INTO academic_term (school_id, term_code, name, start_date, end_date, is_current)
VALUES
    (1, '2024-FIRST-TERM', 'First Term 2024', '2024-09-01', '2024-12-15', TRUE),
    (1, '2024-SECOND-TERM', 'Second Term 2024', '2025-01-08', '2025-03-28', FALSE);

-- Subjects (JSS Level)
INSERT INTO subjects (school_id, subject_code, name, class_level, stage, status)
VALUES
    (1, 'MTH-JSS1', 'Mathematics', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE'),
    (1, 'ENG-JSS1', 'English Language', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE'),
    (1, 'BAS-SCI-JSS1', 'Basic Science', 'JSS_1', 'JUNIOR_SECONDARY', 'ACTIVE');

-- School classes
INSERT INTO school_classes (school_id, class_name, class_level, arm, stage, academic_year, form_teacher_id)
VALUES
    (1, 'JSS 1A', 'JSS_1', 'A', 'JUNIOR_SECONDARY', '2024/2025', 101),  -- Teacher ID 101
    (1, 'JSS 1B', 'JSS_1', 'B', 'JUNIOR_SECONDARY', '2024/2025', 102);

-- Class sections (JSS 1A - Math)
INSERT INTO class_sections (school_id, class_id, subject_id, teacher_id, room, schedule, max_capacity)
VALUES
    (1, 1, 1, 101, 'Room 101', 'Monday 8:00-9:00, Wednesday 10:00-11:00', 45),
    (1, 1, 2, 103, 'Room 102', 'Tuesday 8:00-9:00, Thursday 10:00-11:00', 45);

-- Enrollment (Student 1001 → JSS 1A)
INSERT INTO enrollment (student_id, class_id, enrollment_status)
VALUES (1001, 1, 'ENROLLED');

-- Section enrollment (Student 1001 → Math section)
INSERT INTO section_enrollment (section_id, student_id)
VALUES (1, 1001);

-- Attendance (Student 1001, Math section)
INSERT INTO attendance (section_id, student_id, attendance_date, attendance_status, recorded_by)
VALUES
    (1, 1001, '2024-09-02', 'PRESENT', 101),
    (1, 1001, '2024-09-04', 'ABSENT', 101);

-- Assessment (First Math Test)
INSERT INTO assessment (section_id, term_id, name, assessment_type, max_score, weight, due_date)
VALUES (1, 1, 'First Term Midterm Test', 'TEST', 30.00, 0.30, '2024-10-15');

-- Mark (Student 1001 scored 25/30)
INSERT INTO markbook_entry (assessment_id, student_id, term_id, score_obtained, marked_by)
VALUES (1, 1001, 1, 25.00, 101);