package com.emis.academicservice.exception;

public class StudentNotEnrolledException extends RuntimeException{
  public StudentNotEnrolledException(String msg) {
    super(msg);
  }
}
