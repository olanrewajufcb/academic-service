CREATE UNIQUE INDEX uq_subject_school_code
ON subjects (subject_code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_subject_school_code_grade_level
ON subjects (subject_code, grade_level)
WHERE deleted_at IS NULL;