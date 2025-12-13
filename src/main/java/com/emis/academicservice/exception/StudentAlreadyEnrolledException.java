package com.emis.academicservice.exception;

public class StudentAlreadyEnrolledException extends  RuntimeException {
  public StudentAlreadyEnrolledException(String msg) {
    super(msg);
  }
}
