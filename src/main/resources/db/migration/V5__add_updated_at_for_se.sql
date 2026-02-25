
-- Add this at the top of your counter function:

-- IF TG_TABLE_NAME <> 'section_enrollments' THEN
--     RETURN NULL;
-- END IF;


ALTER TABLE academic_schema.section_enrollments
    ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();
CREATE INDEX idx_section_enrollment_active
    ON academic_schema.section_enrollments(section_id)
    WHERE deleted_at IS NULL;