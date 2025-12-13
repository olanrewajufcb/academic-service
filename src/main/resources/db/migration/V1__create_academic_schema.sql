-- =============================================
-- Academic Service Schema (Nigeria Context)
-- Microservices: No FKs to external services
-- =============================================

-- 0. Create ENUM types
CREATE TYPE school_stage_type AS ENUM (
    'PRE_NURSERY', 'NURSERY', 'PRIMARY',
    'JUNIOR_SECONDARY', 'SENIOR_SECONDARY'
);

CREATE TYPE class_level_type AS ENUM (
    'PRE_NURSERY', 'NURSERY_1', 'NURSERY_2',
    'PRIMARY_1', 'PRIMARY_2', 'PRIMARY_3',
    'PRIMARY_4', 'PRIMARY_5', 'PRIMARY_6',
    'JSS_1', 'JSS_2', 'JSS_3',
    'SSS_1', 'SSS_2', 'SSS_3'
);

CREATE TYPE enrollment_status_type AS ENUM (
    'ENROLLED', 'WAITLISTED', 'DROPPED', 'COMPLETED', 'FAILED'
);

CREATE TYPE attendance_status_type AS ENUM (
    'PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'SICK_LEAVE'
);

CREATE TYPE subject_status_type AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED');
CREATE TYPE assessment_type_type AS ENUM (
    'EXAM', 'TEST', 'ASSIGNMENT', 'PROJECT', 'PRACTICAL', 'PARTICIPATION'
);

-- 1. SUBJECTS
CREATE TABLE subjects (
                          subject_id BIGSERIAL PRIMARY KEY,
                          school_id BIGINT NOT NULL,  -- External FK (validated at app layer)
                          subject_code VARCHAR(20) NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          class_level class_level_type NOT NULL,
                          stage school_stage_type NOT NULL,
                          status subject_status_type DEFAULT 'ACTIVE' NOT NULL,
                          created_at TIMESTAMPTZ DEFAULT NOW(),
                          updated_at TIMESTAMPTZ DEFAULT NOW(),
                          UNIQUE(school_id, subject_code)
);

-- 2. SCHOOL_CLASSES
CREATE TABLE school_classes (
                                class_id BIGSERIAL PRIMARY KEY,
                                school_id BIGINT NOT NULL,  -- External FK
                                school_code BIGINT NOT NULL default '',
                                school_name VARCHAR(50) NOT NULL  default '',
                                class_name VARCHAR(50) NOT NULL,
                                class_level class_level_type NOT NULL,
                                arm VARCHAR(20),
                                stage school_stage_type NOT NULL,
                                academic_year VARCHAR(10) NOT NULL,
                                form_teacher_id BIGINT,  -- External FK (HR service)
                                form_teacher_name VARCHAR(50), -- denormalized (XTner Hr SVC)
                                max_students INTEGER NOT NULL DEFAULT 50,
                                current_students INTEGER NOT NULL DEFAULT 0 CHECK (current_students >= 0),
                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                UNIQUE(school_id, class_name, academic_year),
                                CHECK (current_students <= max_students)
);

-- 3. CLASS_SECTIONS
CREATE TABLE class_sections (
                                section_id BIGSERIAL PRIMARY KEY,
                                school_id BIGINT NOT NULL,  -- Denormalized for performance
                                class_id BIGINT NOT NULL REFERENCES school_classes(class_id) ON DELETE CASCADE,   --  FK (to school_classes, same service)
                                subject_id BIGINT NOT NULL REFERENCES subjects(subject_id) ON DELETE CASCADE, --  FK (to subjects, same service)
                                teacher_id BIGINT,          --  FK (HR service)
                                room VARCHAR(20),
                                schedule VARCHAR(100),
                                max_capacity INTEGER NOT NULL DEFAULT 50 CHECK (max_capacity > 0),
                                current_enrollment INTEGER NOT NULL DEFAULT 0 CHECK (current_enrollment >= 0),
                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                UNIQUE(class_id, subject_id),
                                CHECK (current_enrollment <= max_capacity)
);

-- 4. ENROLLMENT
CREATE TABLE enrollments (
                            enrollment_id BIGSERIAL PRIMARY KEY,
                            student_id BIGINT NOT NULL, -- External FK (Student service)
                            student_number VARCHAR(50),
                            student_name VARCHAR(50),
                            class_id BIGINT NOT NULL,   -- Internal FK
                            enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                            enrollment_status enrollment_status_type DEFAULT 'ENROLLED' NOT NULL,
                            admitted_by BIGINT,
                            admitted_at TIMESTAMPTZ DEFAULT NOW(),
                            rejection_reason VARCHAR(100),
                            created_at TIMESTAMPTZ DEFAULT NOW(),
                            updated_at TIMESTAMPTZ DEFAULT NOW(),
                            UNIQUE(student_id, class_id)
);

-- 5. SECTION_ENROLLMENT
CREATE TABLE section_enrollments (
                                    section_enrollment_id BIGSERIAL PRIMARY KEY,
                                    section_id BIGINT NOT NULL, -- Internal FK
                                    student_id BIGINT NOT NULL, -- External FK
                                    enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                                    created_at TIMESTAMPTZ DEFAULT NOW(),
                                    UNIQUE(section_id, student_id)
);

-- 6. ASSESSMENT
CREATE TABLE assessments (
                            assessment_id BIGSERIAL PRIMARY KEY,
                            section_id BIGINT NOT NULL, -- Internal FK
                            term_id BIGINT NOT NULL,    -- External FK (to academic_term, same service)
                            name VARCHAR(200) NOT NULL,
                            assessment_type assessment_type_type NOT NULL,
                            description TEXT,
                            max_score NUMERIC(5,2) NOT NULL CHECK (max_score > 0),
                            weight NUMERIC(3,2) DEFAULT 1.00 CHECK (weight >= 0 AND weight <= 1),
                            due_date DATE,
                            created_at TIMESTAMPTZ DEFAULT NOW()
                            ADD CONSTRAINT uk_section_term_name
                            UNIQUE (section_id, term_id, name)
);

-- 7. MARKBOOK_ENTRY
CREATE TABLE markbook_entry (
                                mark_entry_id BIGSERIAL PRIMARY KEY,
                                assessment_id BIGINT NOT NULL, -- Internal FK
                                student_id BIGINT NOT NULL,    -- External FK
                                term_id BIGINT NOT NULL,       -- External FK
                                score_obtained NUMERIC(5,2) CHECK (score_obtained >= 0),
                                remark TEXT,
                                marked_at TIMESTAMPTZ DEFAULT NOW(),
                                marked_by BIGINT,  -- External FK (teacher ID)
                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                UNIQUE(assessment_id, student_id)
);

-- 8. ATTENDANCE
CREATE TABLE attendance (
                            attendance_id BIGSERIAL PRIMARY KEY,
                            section_id BIGINT NOT NULL, -- Internal FK
                            student_id BIGINT NOT NULL, -- External FK
                            attendance_date DATE NOT NULL,
                            attendance_status attendance_status_type NOT NULL,
                            notes TEXT,
                            recorded_at TIMESTAMPTZ DEFAULT NOW(),
                            recorded_by BIGINT,  -- External FK (staff ID)
                            created_at TIMESTAMPTZ DEFAULT NOW(),
                            UNIQUE(section_id, student_id, attendance_date)
);

-- 9. ACADEMIC_TERM
CREATE TABLE academic_term (
                               term_id BIGSERIAL PRIMARY KEY,
                               school_id BIGINT NOT NULL,  -- External FK
                               term_code VARCHAR(20) NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               start_date DATE NOT NULL,
                               end_date DATE NOT NULL,
                               is_current BOOLEAN DEFAULT FALSE NOT NULL,
                               created_at TIMESTAMPTZ DEFAULT NOW(),
                               updated_at TIMESTAMPTZ DEFAULT NOW(),
                               UNIQUE(school_id, term_code),
                               CHECK (start_date < end_date)
);

-- 10. TERM_SCORES
CREATE TABLE term_scores (
                             term_score_id BIGSERIAL PRIMARY KEY,
                             student_id BIGINT NOT NULL, -- External FK
                             section_id BIGINT NOT NULL, -- Internal FK
                             term_id BIGINT NOT NULL,    -- Internal FK
                             total_score NUMERIC(7,2),
                             average_score NUMERIC(5,2),
                             position_in_class INTEGER,
                             remarks TEXT,
                             calculated_at TIMESTAMPTZ DEFAULT NOW(),
                             UNIQUE(student_id, section_id, term_id)
);

-- =============================================
-- Indexes (Optimized for Microservices)
-- =============================================

-- Core lookup indexes
CREATE INDEX CONCURRENTLY idx_subjects_school_class
    ON subjects(school_id, class_level, subject_id);
CREATE INDEX idx_school_classes_school_year ON school_classes(school_id, academic_year);
CREATE INDEX idx_class_sections_school ON class_sections(school_id);
CREATE INDEX idx_class_sections_class ON class_sections(class_id);
CREATE INDEX idx_class_sections_subject ON class_sections(subject_id);

-- Enrollment & attendance (high-read)
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_class ON enrollment(class_id);
CREATE INDEX idx_section_enrollment_student ON section_enrollment(student_id);
CREATE INDEX idx_section_enrollment_section ON section_enrollment(section_id);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_attendance_section_date ON attendance(section_id, attendance_date);
CREATE INDEX CONCURRENTLY idx_enrollments_class_active_name
    ON enrollments(class_id, enrollment_status, student_name)
    INCLUDE (student_id, student_number, student_name);

-- Assessment & markbook
CREATE INDEX idx_assessment_section_term ON assessment(section_id, term_id);
CREATE INDEX idx_markbook_student_term ON markbook_entry(student_id, term_id);
CREATE INDEX idx_markbook_assessment ON markbook_entry(assessment_id);

-- Academic term (current term queries)
CREATE INDEX idx_term_current ON academic_term(school_id, is_current)
    WHERE is_current = TRUE;

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
    BEFORE UPDATE ON subjects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_school_classes_updated_at
    BEFORE UPDATE ON school_classes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_class_sections_updated_at
    BEFORE UPDATE ON class_sections FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_academic_term_updated_at
    BEFORE UPDATE ON academic_term FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Enrollment counters (class & section)
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
WHERE school_id = NEW.school_id AND term_id != NEW.term_id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_single_current_term
    BEFORE INSERT OR UPDATE ON academic_term
                         FOR EACH ROW EXECUTE FUNCTION ensure_single_current_term();