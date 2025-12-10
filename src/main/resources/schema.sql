-- Academic Service Tables (Nigeria Context)
-- Using consistent BIGINT IDs for all tables

-- 1. SUBJECTS Table (renamed from COURSE)
CREATE TABLE subjects (
                          subject_id BIGSERIAL PRIMARY KEY,
                          school_id BIGINT NOT NULL,  -- Remove REFERENCES until school table exists
                          subject_code VARCHAR(20) NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          class_level VARCHAR(100),  -- "JSS 1", "SSS 2", "PRIMARY 5"
                          status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          UNIQUE(school_id, subject_code)
);

-- 2. SCHOOL_CLASSES Table (Year/Class Group)
CREATE TABLE school_classes (
                                class_id BIGSERIAL PRIMARY KEY,
                                school_id BIGINT NOT NULL,
                                class_name VARCHAR(50) NOT NULL,  -- "JSS 1A", "SSS 3 Science"
                                class_level VARCHAR(30) NOT NULL CHECK (
                                    class_level IN (
                                        --Nursery
                                                    'NURSERY_1', 'NURSERY_2', 'PRE_NURSERY',

                                        -- Primary
                                                    'PRIMARY_1','PRIMARY_2','PRIMARY_3','PRIMARY_4','PRIMARY_5','PRIMARY_6',

                                        -- Junior Secondary
                                                    'JSS_1','JSS_2','JSS_3',

                                        -- Senior Secondary
                                                    'SSS_1','SSS_2','SSS_3'
                                        )
                                    ),

                                arm VARCHAR(20),  -- "A", "B", "Science", "Arts" (for senior classes)
                                stage VARCHAR(20) CHECK (stage IN ('PRIMARY', 'JUNIOR_SECONDARY', 'SENIOR_SECONDARY'))

                                    academic_year VARCHAR(10) NOT NULL,  -- "2024/2025"
                                form_teacher_id BIGINT,  -- Will be populated when HR service exists
                                max_students INTEGER DEFAULT 50,
                                current_students INTEGER DEFAULT 0,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE(school_id, class_name, academic_year)
);

-- 3. CLASS_SECTIONS Table (Subject Sections within a Class)
CREATE TABLE class_sections (
                                section_id BIGSERIAL PRIMARY KEY,
                                class_id BIGINT NOT NULL REFERENCES school_classes(class_id) ON DELETE CASCADE,
                                subject_id BIGINT NOT NULL REFERENCES subjects(subject_id) ON DELETE CASCADE,
                                teacher_id BIGINT,  -- Subject teacher (from HR service)
                                room VARCHAR(20),
                                schedule VARCHAR(100),  -- "Monday 8:00-9:00, Wednesday 10:00-11:00"
                                max_capacity INTEGER DEFAULT 50,
                                current_enrollment INTEGER DEFAULT 0,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE(class_id, subject_id)
);

-- 4. ENROLLMENT Table (Student -> Class enrollment)
CREATE TABLE enrollment (
                            enrollment_id BIGSERIAL PRIMARY KEY,
                            student_id BIGINT NOT NULL,  -- Will reference students table
                            class_id BIGINT NOT NULL REFERENCES school_classes(class_id) ON DELETE CASCADE,
                            enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            enrollment_status VARCHAR(20) DEFAULT 'ENROLLED'
                                CHECK (enrollment_status IN ('ENROLLED', 'WAITLISTED', 'DROPPED', 'COMPLETED', 'FAILED')),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(student_id, class_id)  -- Student can only be enrolled once per class
);

-- 5. SECTION_ENROLLMENT Table (Student -> Subject Section enrollment)
CREATE TABLE section_enrollment (
                                    section_enrollment_id BIGSERIAL PRIMARY KEY,
                                    section_id BIGINT NOT NULL REFERENCES class_sections(section_id) ON DELETE CASCADE,
                                    student_id BIGINT NOT NULL,  -- Will reference students table
                                    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE(section_id, student_id)  -- Student can only enroll once per subject section
);

-- 6. ASSESSMENT Table
CREATE TABLE assessment (
                            assessment_id BIGSERIAL PRIMARY KEY,
                            section_id BIGINT NOT NULL REFERENCES class_sections(section_id) ON DELETE CASCADE,
                            name VARCHAR(200) NOT NULL,
                            assessment_type VARCHAR(50) NOT NULL
                                CHECK (assessment_type IN ('EXAM', 'TEST', 'ASSIGNMENT', 'PROJECT', 'PRACTICAL', 'PARTICIPATION')),
                            description TEXT,
                            max_score NUMERIC(5,2) NOT NULL CHECK (max_score > 0),
                            weight NUMERIC(3,2) DEFAULT 1.00 CHECK (weight >= 0 AND weight <= 1),
                            due_date DATE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. MARKBOOK_ENTRY Table
CREATE TABLE markbook_entry (
                                mark_entry_id BIGSERIAL PRIMARY KEY,
                                assessment_id BIGINT NOT NULL REFERENCES assessment(assessment_id) ON DELETE CASCADE,
                                student_id BIGINT NOT NULL,
                                score_obtained NUMERIC(5,2) CHECK (score_obtained >= 0),
                                remark TEXT,
                                marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                marked_by BIGINT,  -- Teacher who marked
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE(assessment_id, student_id)
);

-- 8. ATTENDANCE Table
CREATE TABLE attendance (
                            attendance_id BIGSERIAL PRIMARY KEY,
                            section_id BIGINT NOT NULL REFERENCES class_sections(section_id) ON DELETE CASCADE,
                            student_id BIGINT NOT NULL,
                            attendance_date DATE NOT NULL,
                            attendance_status VARCHAR(20) NOT NULL
                                CHECK (attendance_status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'SICK_LEAVE')),
                            notes TEXT,
                            recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            recorded_by BIGINT,  -- Staff who recorded
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(section_id, student_id, attendance_date)
);

-- 9. ACADEMIC_TERM Table
CREATE TABLE academic_term (
                               term_id BIGSERIAL PRIMARY KEY,
                               school_id BIGINT NOT NULL,
                               term_code VARCHAR(20) NOT NULL,  -- "2024-FIRST-TERM"
                               name VARCHAR(100) NOT NULL,  -- "First Term 2024"
                               start_date DATE NOT NULL,
                               end_date DATE NOT NULL,
                               is_current BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE(school_id, term_code),
                               CHECK (end_date > start_date)
);

-- 10. TERM_SCORES Table (Optional - Performance View)
CREATE TABLE term_scores (
                             term_score_id BIGSERIAL PRIMARY KEY,
                             student_id BIGINT NOT NULL,
                             section_id BIGINT NOT NULL REFERENCES class_sections(section_id) ON DELETE CASCADE,
                             academic_term VARCHAR(50) NOT NULL,
                             total_score NUMERIC(7,2),
                             average_score NUMERIC(5,2),
                             position_in_class INTEGER,
                             remarks TEXT,
                             calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE(student_id, section_id, academic_term)
);

-- Create indexes for performance
CREATE INDEX idx_subjects_school ON subjects(school_id);
CREATE INDEX idx_subjects_level ON subjects(class_level);
CREATE INDEX idx_school_classes_school ON school_classes(school_id);
CREATE INDEX idx_school_classes_year ON school_classes(academic_year);
CREATE INDEX idx_class_sections_class ON class_sections(class_id);
CREATE INDEX idx_class_sections_subject ON class_sections(subject_id);
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_class ON enrollment(class_id);
CREATE INDEX idx_section_enrollment_student ON section_enrollment(student_id);
CREATE INDEX idx_section_enrollment_section ON section_enrollment(section_id);
CREATE INDEX idx_assessment_section ON assessment(section_id);
CREATE INDEX idx_markbook_student ON markbook_entry(student_id);
CREATE INDEX idx_markbook_assessment ON markbook_entry(assessment_id);
CREATE INDEX idx_attendance_student ON attendance(student_id);
CREATE INDEX idx_attendance_section ON attendance(section_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_term_school ON academic_term(school_id);
CREATE INDEX idx_term_current ON academic_term(is_current) WHERE is_current = TRUE;

-- Triggers for automatic counts
CREATE OR REPLACE FUNCTION update_class_enrollment()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
UPDATE school_classes
SET current_students = current_students + 1
WHERE class_id = NEW.class_id;
ELSIF TG_OP = 'DELETE' THEN
UPDATE school_classes
SET current_students = current_students - 1
WHERE class_id = OLD.class_id;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_class_enrollment_count
    AFTER INSERT OR DELETE ON enrollment
    FOR EACH ROW EXECUTE FUNCTION update_class_enrollment();

CREATE OR REPLACE FUNCTION update_section_enrollment()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
UPDATE class_sections
SET current_enrollment = current_enrollment + 1
WHERE section_id = NEW.section_id;
ELSIF TG_OP = 'DELETE' THEN
UPDATE class_sections
SET current_enrollment = current_enrollment - 1
WHERE section_id = OLD.section_id;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_section_enrollment_count
    AFTER INSERT OR DELETE ON section_enrollment
    FOR EACH ROW EXECUTE FUNCTION update_section_enrollment();

-- Ensure only one current term per school
CREATE OR REPLACE FUNCTION ensure_single_current_term()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_current THEN
UPDATE academic_term
SET is_current = FALSE
WHERE school_id = NEW.school_id
  AND term_id != NEW.term_id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_single_current_term
    BEFORE INSERT OR UPDATE ON academic_term
                         FOR EACH ROW EXECUTE FUNCTION ensure_single_current_term();