package com.emis.academicservice.exception;

public class InvalidAssessmentException extends RuntimeException {
  public InvalidAssessmentException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
