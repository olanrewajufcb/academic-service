-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================
CREATE SCHEMA IF NOT EXISTS academic_schema OWNER To academic_user;

GRANT USAGE ON SCHEMA academic_schema TO academic_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA academic_schema TO academic_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA academic_schema TO academic_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA academic_schema
GRANT ALL ON TABLES TO academic_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA academic_schema
GRANT ALL ON SEQUENCES TO academic_user;


-- 0. Create ENUM types
DO $$
DECLARE
v_type_exists BOOLEAN;
BEGIN
    -- Check if type exists
SELECT EXISTS (
    SELECT 1 FROM pg_type WHERE typname = 'school_stage_type' AND typnamespace = (
        SELECT oid FROM pg_namespace WHERE nspname = 'academic_schema'
    )
) INTO v_type_exists;

IF NOT v_type_exists THEN
CREATE TYPE academic_schema.school_stage_type AS ENUM (
             'PRE_NURSERY', 'NURSERY', 'PRIMARY','JUNIOR_SECONDARY', 'SENIOR_SECONDARY'
        );
RAISE NOTICE 'Created type: school_stage_type';
ELSE
        RAISE NOTICE 'Type already exists: school_stage_type';
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'grade_level_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.grade_level_type AS ENUM ( 'PRE_NURSERY', 'NURSERY_1', 'NURSERY_2',
    'PRIMARY_1', 'PRIMARY_2', 'PRIMARY_3',
    'PRIMARY_4', 'PRIMARY_5', 'PRIMARY_6',
    'JSS_1', 'JSS_2', 'JSS_3',
    'SSS_1', 'SSS_2', 'SSS_3');
END IF;
END $$;



DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'enrollment_status_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.enrollment_status_type AS ENUM ( 'ENROLLED', 'WAITLISTED',
    'DROPPED', 'COMPLETED', 'FAILED');

END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'attendance_status_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.attendance_status_type AS ENUM (     'PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'SICK_LEAVE'
);
END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'subject_status_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.subject_status_type AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED'

);
END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'assessment_type_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.assessment_type_type AS ENUM ('EXAM', 'TEST', 'ASSIGNMENT',
    'PROJECT', 'PRACTICAL', 'PARTICIPATION'
);
END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'assessment_status_type'
          AND n.nspname = 'academic_schema'
    ) THEN
CREATE TYPE academic_schema.assessment_status_type AS ENUM (
    'PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'CANCELLED', 'APPROVED', 'FAILED'

);
END IF;
END $$;



