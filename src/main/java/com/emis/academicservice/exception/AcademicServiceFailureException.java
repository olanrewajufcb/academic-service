package com.emis.academicservice.exception;


public class AcademicServiceFailureException extends RuntimeException {

  private final String fieldName;
  private final Object rejectedValue;

  public AcademicServiceFailureException(String message) {
    super(message);
    this.fieldName = null;
    this.rejectedValue = null;
  }

  public AcademicServiceFailureException(String message, String fieldName, Object rejectedValue) {
    super(message);
    this.fieldName = fieldName;
    this.rejectedValue = rejectedValue;
  }
}
