DROP INDEX idx_attendance_student_active;

CREATE UNIQUE INDEX uk_attendance_lesson_student_active
    ON academic_schema.student_attendance (lesson_id, student_id)
    WHERE deleted_at IS NULL;