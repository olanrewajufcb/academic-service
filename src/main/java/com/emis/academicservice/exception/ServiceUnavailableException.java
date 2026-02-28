package com.emis.academicservice.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServiceUnavailableException(String msg, String responseBodyAsString) {
        super(msg + ": " + responseBodyAsString);
    }
}