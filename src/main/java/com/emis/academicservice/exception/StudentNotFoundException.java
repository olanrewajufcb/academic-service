package com.emis.academicservice.exception;

public class StudentNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String studentNumber) {
        this((Object) studentNumber);
    }

    public StudentNotFoundException(Long studentId) {
        this((Object) studentId);
    }

    private StudentNotFoundException(Object id) {
        super("Student not found with id: " + String.valueOf(id));
    }
}