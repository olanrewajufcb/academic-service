package com.emis.academicservice.exception;

import lombok.Getter;

@Getter
public class AlreadyExistsException extends  RuntimeException {

    private final String fieldName;
    private final Object rejectedValue;

    public AlreadyExistsException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public AlreadyExistsException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }}
