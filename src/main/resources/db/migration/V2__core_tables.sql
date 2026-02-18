-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================

-- 1. ACADEMIC_TERM
CREATE TABLE academic_schema.academic_term (
                                               term_id BIGSERIAL PRIMARY KEY,
                                               school_id BIGINT NOT NULL,  -- External FK
                                               term_code VARCHAR(20) NOT NULL,
                                               name VARCHAR(100) NOT NULL,
                                               start_date DATE NOT NULL,
                                               end_date DATE NOT NULL,
                                               is_current BOOLEAN DEFAULT FALSE NOT NULL,
                                               is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                               deleted_at TIMESTAMPTZ DEFAULT NULL,
                                               created_at TIMESTAMPTZ DEFAULT NOW(),
                                               updated_at TIMESTAMPTZ DEFAULT NOW(),
                                               CHECK (start_date < end_date)
);

-- 2. SUBJECTS
CREATE TABLE academic_schema.subjects (
                                          subject_id BIGSERIAL PRIMARY KEY,
                                          school_id BIGINT NOT NULL,  -- External FK (validated at app layer)
                                          school_code VARCHAR(20) NOT NULL,
                                          subject_code VARCHAR(20) NOT NULL,
                                          name VARCHAR(200) NOT NULL,
                                          description TEXT,
                                          grade_level VARCHAR(30) NOT NULL,
                                          stage VARCHAR(20) NOT NULL,
                                          status VARCHAR(15) DEFAULT 'ACTIVE' NOT NULL,
                                          is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                          deleted_at TIMESTAMPTZ DEFAULT NULL,
                                          created_at TIMESTAMPTZ DEFAULT NOW(),
                                          updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. SCHOOL_CLASSES
CREATE TABLE academic_schema.school_classes (
                                                class_id BIGSERIAL PRIMARY KEY,
                                                school_id BIGINT NOT NULL,  -- External FK
                                                school_code VARCHAR(20) NOT NULL,   -- Denormalized for performance
                                                school_name VARCHAR(50) NOT NULL  default '',   -- Denormalized for performance
                                                class_name VARCHAR(50) NOT NULL,
                                                class_code VARCHAR(64) GENERATED ALWAYS AS (
                                                    school_code || '-' ||
                                                    REPLACE(LOWER(class_name), ' ', '-') || '-' ||
                                                    SUBSTRING(academic_year FROM 1 FOR 4)
                                                    ) STORED,
                                                grade_level VARCHAR(20) NOT NULL,
                                                arm VARCHAR(20),
                                                stage VARCHAR(30) NOT NULL,
                                                academic_year VARCHAR(10) NOT NULL,
                                                form_teacher_id BIGINT,  -- External FK (HR service)
                                                form_teacher_name VARCHAR(50) DEFAULT '', -- denormalized (XTner Hr SVC)
                                                form_teacher_validated BOOLEAN DEFAULT FALSE,
                                                max_students INTEGER NOT NULL DEFAULT 50,
                                                current_students INTEGER NOT NULL DEFAULT 0 CHECK (current_students >= 0),
                                                is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(school_id, class_name, academic_year, deleted_at),
                                                CHECK (current_students <= max_students)
);

-- 4. CLASS_SECTIONS
CREATE TABLE academic_schema.class_sections (
                                                section_id BIGSERIAL PRIMARY KEY,
                                                school_id BIGINT NOT NULL,  -- Denormalized for performance
                                                school_code VARCHAR(20) NOT NULL DEFAULT '',
                                                class_id BIGINT NOT NULL REFERENCES academic_schema.school_classes(class_id) ON DELETE RESTRICT,   -- FK (to school_classes, same service) - no cascade
                                                subject_id BIGINT NOT NULL REFERENCES academic_schema.subjects(subject_id) ON DELETE RESTRICT, -- FK (to subjects, same service) - no cascade
                                                teacher_id BIGINT,          -- FK (HR service)
                                                staff_code VARCHAR, --denormalized (XTner Hr SVC)
                                                teacher_name VARCHAR(100) DEFAULT '',
                                                teacher_validated BOOLEAN DEFAULT FALSE,
                                                room VARCHAR(20),
                                                schedule VARCHAR(100),
                                                max_capacity INTEGER NOT NULL DEFAULT 50 CHECK (max_capacity > 0),
                                                current_enrollment INTEGER NOT NULL DEFAULT 0 CHECK (current_enrollment >= 0),
                                                term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,  -- "First Term 2024"
                                                is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(class_id, subject_id, deleted_at),
                                                CHECK (current_enrollment <= max_capacity)
);

-- 5. ENROLLMENT
CREATE TABLE academic_schema.enrollments (
                                             enrollment_id BIGSERIAL PRIMARY KEY,
                                             student_id BIGINT NOT NULL, -- External FK (Student service)
                                             student_number VARCHAR(50) NOT NULL,
                                             student_name VARCHAR(50) NOT NULL,
                                             class_id BIGINT NOT NULL REFERENCES academic_schema.school_classes(class_id) ON DELETE RESTRICT,   -- No cascade
                                             enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                                             enrollment_status VARCHAR(15) DEFAULT 'ENROLLED' NOT NULL,
                                             admitted_by BIGINT,
                                             idempotency_key VARCHAR(50) NOT NULL,
                                             admitted_at TIMESTAMPTZ DEFAULT NOW(),
                                             rejection_reason VARCHAR(100),
                                             is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             created_at TIMESTAMPTZ DEFAULT NOW(),
                                             updated_at TIMESTAMPTZ DEFAULT NOW(),
                                             UNIQUE(idempotency_key)
);

-- 6. SECTION_ENROLLMENT
CREATE TABLE academic_schema.section_enrollments (
                                                     section_enrollment_id BIGSERIAL PRIMARY KEY,
                                                     section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                                     student_id BIGINT NOT NULL, -- External FK
                                                     student_number VARCHAR(50) NOT NULL,
                                                     enrollment_date TIMESTAMPTZ DEFAULT NOW(),
                                                     is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                                     deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                     created_at TIMESTAMPTZ DEFAULT NOW(),
                                                     UNIQUE(section_id, student_id, deleted_at)
);

-- 7. ASSESSMENT
CREATE TABLE academic_schema.assessments (
                                             assessment_id BIGSERIAL PRIMARY KEY,
                                             section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                             term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,    -- No cascade
                                             school_id BIGINT NOT NULL,
                                             school_code VARCHAR(20),
                                             name VARCHAR(200) NOT NULL,
                                             assessment_type VARCHAR(50) NOT NULL,
                                             description TEXT,
                                             max_score NUMERIC(5,2) NOT NULL CHECK (max_score > 0),
                                             weight NUMERIC(3,2) DEFAULT 1.00 CHECK (weight >= 0 AND weight <= 1),
                                             due_date DATE,
                                             status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
                                             is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             created_at TIMESTAMPTZ DEFAULT NOW(),
                                             teacher_id BIGINT, -- External FK to hr staff
                                             created_by VARCHAR, -- to be retrieved from JWT
                                             CONSTRAINT uk_section_term_name
                                                 UNIQUE (section_id, term_id, name, deleted_at)
);

-- 8. MARKBOOK_ENTRY
CREATE TABLE academic_schema.markbook_entry (
                                                mark_entry_id BIGSERIAL PRIMARY KEY,
                                                assessment_id BIGINT NOT NULL REFERENCES academic_schema.assessments(assessment_id) ON DELETE RESTRICT, -- No cascade
                                                student_id BIGINT NOT NULL,    -- External FK
                                                term_id BIGINT NOT NULL REFERENCES academic_schema.academic_term(term_id) ON DELETE RESTRICT,       -- No cascade
                                                score_obtained NUMERIC(5,2) CHECK (score_obtained >= 0),
                                                score_percentage NUMERIC(5,2),
                                                remark TEXT,
                                                marked_at TIMESTAMPTZ DEFAULT NOW(),
                                                marked_by BIGINT,  -- External FK (teacher ID)
                                                is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                                deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                created_at TIMESTAMPTZ DEFAULT NOW(),
                                                UNIQUE(assessment_id, student_id, deleted_at)
);

-- 9. ATTENDANCE
CREATE TABLE academic_schema.attendance (
                                            attendance_id BIGSERIAL PRIMARY KEY,
                                            section_id BIGINT NOT NULL REFERENCES academic_schema.class_sections(section_id) ON DELETE RESTRICT, -- No cascade
                                            student_id BIGINT NOT NULL, -- External FK
                                            student_number VARCHAR(100) NOT NULL,
                                            student_name VARCHAR(100) NOT NULL,
                                            school_code VARCHAR(20) NOT NULL,
                                            attendance_date DATE NOT NULL,
                                            attendance_status VARCHAR(20) NOT NULL,
                                            notes TEXT,
                                            recorded_at TIMESTAMPTZ DEFAULT NOW(),
                                            recorded_by BIGINT,  -- External FK (staff ID)
                                            is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
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
                                             is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             calculated_at TIMESTAMPTZ DEFAULT NOW(),
                                             UNIQUE(student_id, section_id, term_id, deleted_at),
                                             CHECK (total_score BETWEEN 0 AND 100)

);

CREATE TABLE academic_schema.consumed_events (
                                                  event_id UUID PRIMARY KEY,
                                                  event_type VARCHAR(100),
                                                  consumed_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE academic_schema.outbox_events (
                                         outbox_id BIGSERIAL PRIMARY KEY,
                                         event_id UUID NOT NULL,
                                         aggregate_type VARCHAR(50) NOT NULL,
                                         aggregate_id VARCHAR(100) NOT NULL,
                                         event_type VARCHAR(100) NOT NULL,
                                         topic VARCHAR(200) NOT NULL,
                                         payload JSONB NOT NULL,
                                         status VARCHAR(20) DEFAULT 'PENDING',
                                         retry_count INT DEFAULT 0,
                                         created_at TIMESTAMPTZ DEFAULT NOW(),
                                         published_at TIMESTAMPTZ
);
