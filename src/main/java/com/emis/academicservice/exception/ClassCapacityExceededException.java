package com.emis.academicservice.exception;

public class ClassCapacityExceededException extends RuntimeException {
  public ClassCapacityExceededException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
