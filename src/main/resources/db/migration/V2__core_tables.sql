-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================

-- 9. ACADEMIC_TERM
CREATE TABLE academic_schema.academic_term (
                                               term_id BIGSERIAL PRIMARY KEY,
                                               school_id BIGINT NOT NULL,  -- External FK
                                               term_code VARCHAR(20) NOT NULL,
                                               name VARCHAR(100) NOT NULL,
                                               start_date DATE NOT NULL,
                                               end_date DATE NOT NULL,
                                               is_current BOOLEAN DEFAULT FALSE NOT NULL,
                                               deleted_at TIMESTAMPTZ DEFAULT NULL,
                                               created_at TIMESTAMPTZ DEFAULT NOW(),
                                               updated_at TIMESTAMPTZ DEFAULT NOW(),
                                               CHECK (start_date < end_date)
);

-- 1. SUBJECTS
CREATE TABLE academic_schema.subjects (
                                          subject_id BIGSERIAL PRIMARY KEY,
                                          school_id BIGINT NOT NULL,  -- External FK (validated at app layer)
                                          subject_code VARCHAR(20) NOT NULL,
                                          name VARCHAR(200) NOT NULL,
                                          description TEXT,
                                          grade_level grade_level_type NOT NULL,
                                          stage school_stage_type NOT NULL,
                                          status subject_status_type DEFAULT 'ACTIVE' NOT NULL,
                                          deleted_at TIMESTAMPTZ DEFAULT NULL,
                                          created_at TIMESTAMPTZ DEFAULT NOW(),
                                          updated_at TIMESTAMPTZ DEFAULT NOW(),
                                          UNIQUE(school_id, subject_code, deleted_at)
);

-- 2. SCHOOL_CLASSES
CREATE TABLE academic_schema.school_classes (
                                                class_id BIGSERIAL PRIMARY KEY,
                                                school_id BIGINT NOT NULL,  -- External FK
                                                school_code VARCHAR(20) NOT NULL default '',   -- Denormalized for performance
                                                school_name VARCHAR(50) NOT NULL  default '',   -- Denormalized for performance
                                                school_validated BOOLEAN DEFAULT FALSE,
                                                last_school_validation TIMESTAMPTZ,
                                                class_name VARCHAR(50) NOT NULL,
                                                class_code VARCHAR(64) GENERATED ALWAYS AS (
                                                    school_code || '-' ||
                                                    REPLACE(LOWER(class_name), ' ', '-') || '-' ||
                                                    SUBSTRING(academic_year FROM 1 FOR 4)
                                                    ) STORED,
                                                grade_level grade_level_type NOT NULL,
                                                arm VARCHAR(20),
                                                stage school_stage_type NOT NULL,
                                                academic_year VARCHAR(10) NOT NULL,
                                                form_teacher_id BIGINT,  -- External FK (HR service)
                                                form_teacher_name VARCHAR(50) DEFAULT '', -- denormalized (XTner Hr SVC)
                                                form_teacher_validated BOOLEAN DEFAULT FALSE,
                                                max_students INTEGER NOT NULL DEFAULT 50,
                                                current_students INTEGER NOT NULL DEFAULT 0 CHECK (current_students >= 0),
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(school_id, class_name, academic_year, deleted_at),
                                                CHECK (current_students <= max_students)
);

-- 3. CLASS_SECTIONS
CREATE TABLE academic_schema.class_sections (
                                                section_id BIGSERIAL PRIMARY KEY,
                                                school_id BIGINT NOT NULL,  -- Denormalized for performance
                                                school_code VARCHAR(20) NOT NULL DEFAULT '',

                                                class_id BIGINT NOT NULL REFERENCES academic_schema.school_classes(class_id) ON DELETE RESTRICT,   -- FK (to school_classes, same service) - no cascade
                                                subject_id BIGINT NOT NULL REFERENCES academic_schema.subjects(subject_id) ON DELETE RESTRICT, -- FK (to subjects, same service) - no cascade
                                                teacher_id BIGINT,          -- FK (HR service)
                                                teacher_name VARCHAR(100) DEFAULT '',
                                                teacher_validated BOOLEAN DEFAULT FALSE,

                                                room VARCHAR(20),
                                                schedule VARCHAR(100),
                                                max_capacity INTEGER NOT NULL DEFAULT 50 CHECK (max_capacity > 0),
                                                current_enrollment INTEGER NOT NULL DEFAULT 0 CHECK (current_enrollment >= 0),
                                                term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,  -- "First Term 2024"
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(class_id, subject_id, deleted_at),
                                                CHECK (current_enrollment <= max_capacity)
);

-- 4. ENROLLMENT
CREATE TABLE academic_schema.enrollments (
                                             enrollment_id BIGSERIAL PRIMARY KEY,
                                             student_id BIGINT NOT NULL, -- External FK (Student service)
                                             student_number VARCHAR(50) NOT NULL,
                                             student_name VARCHAR(50) NOT NULL,
                                             class_id BIGINT NOT NULL REFERENCES academic_schema.school_classes(class_id) ON DELETE RESTRICT,   -- No cascade
                                             enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                                             enrollment_status enrollment_status_type DEFAULT 'ENROLLED' NOT NULL,
                                             admitted_by BIGINT,
                                             admitted_at TIMESTAMPTZ DEFAULT NOW(),
                                             rejection_reason VARCHAR(100),
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             created_at TIMESTAMPTZ DEFAULT NOW(),
                                             updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. SECTION_ENROLLMENT
CREATE TABLE academic_schema.section_enrollments (
                                                     section_enrollment_id BIGSERIAL PRIMARY KEY,
                                                     section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                                     student_id BIGINT NOT NULL, -- External FK
                                                     enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                                                     deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                     created_at TIMESTAMPTZ DEFAULT NOW(),
                                                     UNIQUE(section_id, student_id, deleted_at)
);

-- 6. ASSESSMENT
CREATE TABLE academic_schema.assessments (
                                             assessment_id BIGSERIAL PRIMARY KEY,
                                             section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                             term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,    -- No cascade
                                             name VARCHAR(200) NOT NULL,
                                             assessment_type assessment_type_type NOT NULL,
                                             description TEXT,
                                             max_score NUMERIC(5,2) NOT NULL CHECK (max_score > 0),
                                             weight NUMERIC(3,2) DEFAULT 1.00 CHECK (weight >= 0 AND weight <= 1),
                                             due_date DATE,
                                             status assessment_status_type DEFAULT 'PENDING' NOT NULL,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             created_at TIMESTAMPTZ DEFAULT NOW(),
                                             CONSTRAINT uk_section_term_name
                                                 UNIQUE (section_id, term_id, name, deleted_at)
);

-- 7. MARKBOOK_ENTRY
CREATE TABLE academic_schema.markbook_entry (
                                                mark_entry_id BIGSERIAL PRIMARY KEY,
                                                assessment_id BIGINT NOT NULL REFERENCES academic_schema.assessments(assessment_id) ON DELETE RESTRICT, -- No cascade
                                                student_id BIGINT NOT NULL,    -- External FK
                                                term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,       -- No cascade
                                                score_obtained NUMERIC(5,2) CHECK (score_obtained >= 0),
                                                remark TEXT,
                                                marked_at TIMESTAMPTZ DEFAULT NOW(),
                                                marked_by BIGINT,  -- External FK (teacher ID)
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(assessment_id, student_id, deleted_at)
);

-- 8. ATTENDANCE
CREATE TABLE academic_schema.attendance (
                                            attendance_id BIGSERIAL PRIMARY KEY,
                                            section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                            student_id BIGINT NOT NULL, -- External FK
                                            attendance_date DATE NOT NULL,
                                            attendance_status attendance_status_type NOT NULL,
                                            notes TEXT,
                                            recorded_at TIMESTAMPTZ DEFAULT NOW(),
                                            recorded_by BIGINT,  -- External FK (staff ID)
                                            deleted_at TIMESTAMPTZ DEFAULT NULL,
                                            created_at TIMESTAMPTZ DEFAULT NOW(),
                                            UNIQUE(section_id, student_id, attendance_date, deleted_at),
                                            CHECK (attendance_date <= CURRENT_DATE)

);

-- 10. TERM_SCORES
CREATE TABLE academic_schema.term_scores (
                                             term_score_id BIGSERIAL PRIMARY KEY,
                                             student_id BIGINT NOT NULL, -- External FK
                                             section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                             term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,    -- No cascade
                                             total_score NUMERIC(7,2),
                                             average_score NUMERIC(5,2),
                                             position_in_class INTEGER,
                                             remarks TEXT,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             calculated_at TIMESTAMPTZ DEFAULT NOW(),
                                             UNIQUE(student_id, section_id, term_id, deleted_at),
                                             CHECK (total_score BETWEEN 0 AND 100)

);

