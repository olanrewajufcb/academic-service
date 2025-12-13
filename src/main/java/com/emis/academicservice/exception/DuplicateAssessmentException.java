package com.emis.academicservice.exception;

public class DuplicateAssessmentException extends RuntimeException {
  public DuplicateAssessmentException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
