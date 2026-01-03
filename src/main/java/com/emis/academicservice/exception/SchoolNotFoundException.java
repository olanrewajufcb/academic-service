package com.emis.academicservice.exception;

import lombok.Getter;

@Getter
public class SchoolNotFoundException extends RuntimeException {
    private final String fieldName;
    private final Object searchValue;

    public SchoolNotFoundException(String message) {
        super(message);
        this.fieldName = null;
        this.searchValue = null;
    }

    public SchoolNotFoundException(String message, String fieldName, Object searchValue) {
        super(message);
        this.fieldName = fieldName;
        this.searchValue = searchValue;
    }
}