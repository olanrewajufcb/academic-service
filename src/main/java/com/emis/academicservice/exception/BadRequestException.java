package com.emis.academicservice.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String msg) {
      super(msg);
  }
}
