
-- =============================================
-- Triggers
-- =============================================

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_subjects_updated_at
    BEFORE UPDATE ON academic_schema.subjects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_school_classes_updated_at
    BEFORE UPDATE ON academic_schema.school_classes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_class_sections_updated_at
    BEFORE UPDATE ON academic_schema.class_sections FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_academic_term_updated_at
    BEFORE UPDATE ON academic_schema.academic_term FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enrollments_updated_at
    BEFORE UPDATE ON academic_schema.enrollments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_section_enrollments_updated_at
    BEFORE UPDATE ON academic_schema.section_enrollments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_assessments_updated_at
    BEFORE UPDATE ON academic_schema.assessments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_markbook_entry_updated_at
    BEFORE UPDATE ON academic_schema.markbook_entry FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendance_updated_at
    BEFORE UPDATE ON academic_schema.student_attendance FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_term_scores_updated_at
    BEFORE UPDATE ON academic_schema.term_scores FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Enrollment counters (class & section) - only update for non-deleted records

CREATE OR REPLACE FUNCTION update_class_enrollment()
RETURNS TRIGGER AS $$
BEGIN
    -- Recalculate old class if changed
    IF TG_OP = 'UPDATE' AND OLD.class_id IS DISTINCT FROM NEW.class_id THEN
UPDATE academic_schema.school_classes
SET current_students = (
    SELECT COUNT(*)
    FROM academic_schema.enrollments
    WHERE class_id = OLD.class_id
      AND deleted_at IS NULL
      AND enrollment_status = 'ENROLLED'
)
WHERE class_id = OLD.class_id;
END IF;

    -- Recalculate new/current class
UPDATE academic_schema.school_classes
SET current_students = (
    SELECT COUNT(*)
    FROM academic_schema.enrollments
    WHERE class_id = COALESCE(NEW.class_id, OLD.class_id)
      AND deleted_at IS NULL
      AND enrollment_status = 'ENROLLED'
)
WHERE class_id = COALESCE(NEW.class_id, OLD.class_id);

RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_class_enrollment_count
    AFTER INSERT OR UPDATE OR DELETE
                    ON academic_schema.enrollments
                        FOR EACH ROW EXECUTE FUNCTION update_class_enrollment();


CREATE OR REPLACE FUNCTION update_section_enrollment()
RETURNS TRIGGER AS $$
BEGIN
UPDATE academic_schema.class_sections
SET current_enrollment = (
    SELECT COUNT(*)
    FROM academic_schema.section_enrollments
    WHERE section_id = COALESCE(NEW.section_id, OLD.section_id)
      AND deleted_at IS NULL
)
WHERE section_id = COALESCE(NEW.section_id, OLD.section_id);

RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_section_enrollment_count
    AFTER INSERT OR UPDATE OR DELETE
                    ON academic_schema.section_enrollments
                        FOR EACH ROW EXECUTE FUNCTION update_section_enrollment();
