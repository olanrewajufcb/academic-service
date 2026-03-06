

-- =============================================
-- Indexes (Optimized for Microservices with Soft Delete)
-- =============================================

CREATE INDEX idx_school_classes_school_year_not_deleted
    ON academic_schema.school_classes(school_id, academic_year)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_class_sections_school_not_deleted
    ON academic_schema.class_sections(school_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_class_sections_class_not_deleted
    ON academic_schema.class_sections(class_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_class_sections_subject_not_deleted
    ON academic_schema.class_sections(subject_id)
    WHERE deleted_at IS NULL;

-- Enrollment & attendance (high-read, excluding soft-deleted records)
CREATE INDEX idx_enrollment_student_not_deleted
    ON academic_schema.enrollments(student_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_enrollment_class_not_deleted
    ON academic_schema.enrollments(class_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_section_enrollment_student_not_deleted
    ON academic_schema.section_enrollments(student_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_section_enrollment_section_not_deleted
    ON academic_schema.section_enrollments(section_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_enrollments_class_active_name_not_deleted
    ON academic_schema.enrollments(class_id, enrollment_status, student_name)
    INCLUDE (student_id, student_number, student_name)
    WHERE deleted_at IS NULL;

-- Assessment & markbook (excluding soft-deleted records)
CREATE INDEX idx_assessment_section_term_not_deleted
    ON academic_schema.assessments(section_id, term_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_markbook_student_term_not_deleted
    ON academic_schema.markbook_entry(student_id, term_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_markbook_assessment_not_deleted
    ON academic_schema.markbook_entry(assessment_id)
    WHERE deleted_at IS NULL;

-- Academic term (current term queries, excluding soft-deleted records)
CREATE INDEX idx_term_current_not_deleted
    ON academic_schema.academic_term(school_id, is_current)
    WHERE is_current = TRUE AND deleted_at IS NULL;

-- Soft delete indexes
CREATE INDEX idx_subjects_deleted_at ON academic_schema.subjects(deleted_at);
CREATE INDEX idx_school_classes_deleted_at ON academic_schema.school_classes(deleted_at);
CREATE INDEX idx_class_sections_deleted_at ON academic_schema.class_sections(deleted_at);
CREATE INDEX idx_academic_term_deleted_at ON academic_schema.academic_term(deleted_at);
CREATE INDEX idx_enrollments_deleted_at ON academic_schema.enrollments(deleted_at);
CREATE INDEX idx_section_enrollments_deleted_at ON academic_schema.section_enrollments(deleted_at);
CREATE INDEX idx_assessments_deleted_at ON academic_schema.assessments(deleted_at);
CREATE INDEX idx_markbook_entry_deleted_at ON academic_schema.markbook_entry(deleted_at);
CREATE INDEX idx_attendance_deleted_at ON academic_schema.student_attendance(deleted_at);
CREATE INDEX idx_term_scores_deleted_at ON academic_schema.term_scores(deleted_at);



-- Index for school_classes table to support the academic_year filter
CREATE INDEX idx_school_classes_academic_year ON school_classes(academic_year) WHERE deleted_at IS NULL;


-- Index for subjects table to support subject_id join
CREATE INDEX idx_subjects_subject_id ON class_sections(class_id) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_subject_school_code_grade
    ON academic_schema.subjects (school_id, subject_code, grade_level)
    WHERE deleted_at IS NULL;


CREATE UNIQUE INDEX uk_term_per_school_year
    ON academic_schema.academic_term (school_id, academic_year, term_code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_one_current_term_per_school
    ON academic_schema.academic_term (school_id)
    WHERE is_current = TRUE AND deleted_at IS NULL;

-- to be reviewed later
CREATE INDEX idx_term_school_year_active
    ON academic_schema.academic_term(school_id, academic_year)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_attendance_student_not_deleted
    ON academic_schema.student_attendance(student_id)
    WHERE deleted_at IS NULL;



CREATE UNIQUE INDEX uk_enrollment_active
    ON academic_schema.enrollments (student_id, class_id)
    WHERE deleted_at IS NULL AND enrollment_status = 'ENROLLED';

CREATE UNIQUE INDEX uk_academic_term_active
    ON academic_schema.academic_term (school_id, term_code)
    WHERE deleted_at IS NULL;




-- Index for class_sections to support class_id join
CREATE INDEX idx_class_sections_class_id ON class_sections(class_id) WHERE deleted_at IS NULL;




CREATE INDEX idx_attendance_student_number_active
    ON academic_schema.student_attendance(student_number, lesson_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_attendance_section_term_active
    ON academic_schema.student_attendance(lesson_id, student_number)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_lessons_section_term_active
    ON academic_schema.lessons(section_id, term_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_section_enrollment_active
    ON academic_schema.section_enrollments(section_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_section_student_active
    ON academic_schema.section_enrollments(section_id, student_id)
    WHERE deleted_at IS NULL;