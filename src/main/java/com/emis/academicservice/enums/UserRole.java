package com.emis.academicservice.enums;


public enum UserRole {

    SYSTEM_ADMIN,
    STATE_ADMIN,
    LGA_ADMIN,
    SCHOOL_ADMIN,
    SCHOOL_STAFF,
    SCHOOL_TEACHER,
    PARENT,
    STUDENT,
    UNKNOWN;

    public static  UserRole fromString(String role) {

        try{
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

    }

}
