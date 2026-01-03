package com.emis.academicservice.exception;

import lombok.Getter;

@Getter
public class ArgumentIsNullException extends  RuntimeException {
    private final String fieldName;

    public ArgumentIsNullException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ArgumentIsNullException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }
}
