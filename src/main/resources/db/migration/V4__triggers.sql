-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================




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
    BEFORE UPDATE ON academic_schema.attendance FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_term_scores_updated_at
    BEFORE UPDATE ON academic_schema.term_scores FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Enrollment counters (class & section) - only update for non-deleted records
CREATE OR REPLACE FUNCTION update_class_enrollment()
RETURNS TRIGGER AS $$
BEGIN
    -- INSERT
    IF TG_OP = 'INSERT' AND NEW.deleted_at IS NULL THEN
UPDATE academic_schema.school_classes
SET current_students = current_students + 1
WHERE class_id = NEW.class_id;
END IF;

    -- SOFT DELETE
    IF TG_OP = 'UPDATE' AND OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
UPDATE academic_schema.school_classes
SET current_students = current_students - 1
WHERE class_id = OLD.class_id;
END IF;

    -- RESTORE
    IF TG_OP = 'UPDATE' AND OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
UPDATE academic_schema.school_classes
SET current_students = current_students + 1
WHERE class_id = NEW.class_id;
END IF;

RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_class_enrollment_count
    AFTER INSERT OR DELETE ON academic_schema.enrollments
FOR EACH ROW EXECUTE FUNCTION update_class_enrollment();

CREATE OR REPLACE FUNCTION update_section_enrollment()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.deleted_at IS NULL THEN
UPDATE academic_schema.class_sections
SET current_enrollment = current_enrollment + 1
WHERE section_id = NEW.section_id;
END IF;
  -- SOFT DELETE
       IF TG_OP = 'UPDATE' AND OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
UPDATE academic_schema.class_sections
SET current_enrollment = current_enrollment - 1
WHERE section_id = OLD.section_id;
END IF;
 -- RESTORE
    IF TG_OP = 'UPDATE' AND OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN

UPDATE academic_schema.class_sections
SET current_enrollment = current_enrollment + 1
WHERE section_id = OLD.section_id;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_section_enrollment_count
    AFTER INSERT OR DELETE ON academic_schema.section_enrollments
FOR EACH ROW EXECUTE FUNCTION update_section_enrollment();

-- Ensure only one current term per school
CREATE OR REPLACE FUNCTION ensure_single_current_term()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_current AND NEW.deleted_at IS NULL THEN
UPDATE academic_schema.academic_term
SET is_current = FALSE
WHERE school_id = NEW.school_id AND term_id != NEW.term_id AND deleted_at IS NULL;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_single_current_term
    BEFORE INSERT OR UPDATE ON academic_schema.academic_term
                         FOR EACH ROW EXECUTE FUNCTION ensure_single_current_term();
