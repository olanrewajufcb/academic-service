-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================

-- =============================================
-- Indexes (Optimized for Microservices with Soft Delete)
-- =============================================

-- Core lookup indexes (excluding soft-deleted records)
CREATE INDEX idx_subjects_school_class_not_deleted
    ON academic_schema.subjects(school_id, grade_level, subject_id)
    WHERE deleted_at IS NULL;
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
CREATE INDEX idx_attendance_student_date_not_deleted
    ON academic_schema.attendance(student_id, attendance_date)
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
CREATE INDEX idx_attendance_deleted_at ON academic_schema.attendance(deleted_at);
CREATE INDEX idx_term_scores_deleted_at ON academic_schema.term_scores(deleted_at);
CREATE INDEX idx_attendance_section_date_active
    ON academic_schema.attendance(section_id, attendance_date)
    WHERE deleted_at IS NULL;


CREATE UNIQUE INDEX uk_enrollment_active
    ON academic_schema.enrollments (student_id, class_id)
    WHERE deleted_at IS NULL AND enrollment_status = 'ENROLLED';

CREATE UNIQUE INDEX uk_academic_term_active
    ON academic_schema.academic_term (school_id, term_code)
    WHERE deleted_at IS NULL;
