-- =============================================
-- Academic Service Schema (Nigeria Context) - Soft Delete Version
-- Microservices: No FKs to external services
-- =============================================

-- 0. Create ENUM types
CREATE TYPE academic_schema.school_stage_type AS ENUM (
    'PRE_NURSERY', 'NURSERY', 'PRIMARY',
    'JUNIOR_SECONDARY', 'SENIOR_SECONDARY'
);

CREATE TYPE academic_schema.grade_level_type AS ENUM (
    'PRE_NURSERY', 'NURSERY_1', 'NURSERY_2',
    'PRIMARY_1', 'PRIMARY_2', 'PRIMARY_3',
    'PRIMARY_4', 'PRIMARY_5', 'PRIMARY_6',
    'JSS_1', 'JSS_2', 'JSS_3',
    'SSS_1', 'SSS_2', 'SSS_3'
);

CREATE TYPE academic_schema.enrollment_status_type AS ENUM (
    'ENROLLED', 'WAITLISTED', 'DROPPED', 'COMPLETED', 'FAILED'
);

CREATE TYPE academic_schema.attendance_status_type AS ENUM (
    'PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'SICK_LEAVE'
);

CREATE TYPE academic_schema.subject_status_type AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED');
CREATE TYPE academic_schema.assessment_type_type AS ENUM (
    'EXAM', 'TEST', 'ASSIGNMENT', 'PROJECT', 'PRACTICAL', 'PARTICIPATION'
);

CREATE TYPE academic_schema.assessment_status_type AS ENUM (
    'PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'CANCELLED', 'APPROVED', 'FAILED'
);