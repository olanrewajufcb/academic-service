package com.emis.academicservice.exception;


public class AssessmentCreationException extends RuntimeException {
  public AssessmentCreationException(
      String msg, Throwable ex) {
    super(msg, ex);
  }
}
