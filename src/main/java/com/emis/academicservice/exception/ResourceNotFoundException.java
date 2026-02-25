package com.emis.academicservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String studentNumber) {
        this((Object) studentNumber);
    }

    public ResourceNotFoundException(Long studentId) {
        this((Object) studentId);
    }

    private ResourceNotFoundException(Object id) {
        super("Student not found with id: " + String.valueOf(id));
    }
}