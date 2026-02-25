ALTER TABLE academic_schema.section_enrollments
DROP CONSTRAINT section_enrollments_section_id_student_id_deleted_at_key;


CREATE UNIQUE INDEX uk_section_student_active
    ON academic_schema.section_enrollments(section_id, student_id)
    WHERE deleted_at IS NULL;



--UNIQUE(section_id, student_id, deleted_at) --this is to be dropped in section_enrollments
