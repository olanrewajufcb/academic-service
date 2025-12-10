package com.emis.academicservice.exception;

public class SchoolServiceUnavailableException extends RuntimeException {
    public SchoolServiceUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SchoolServiceUnavailableException(String msg, String responseBodyAsString) {
        super(msg + ": " + responseBodyAsString);
    }
}