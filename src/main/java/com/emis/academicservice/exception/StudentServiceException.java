package com.emis.academicservice.exception;

public class StudentServiceException extends RuntimeException {
  public StudentServiceException(String msg, Throwable cause) {
      super(msg, cause);
  }
}
