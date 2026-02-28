package com.emis.academicservice.exception;

public class ServiceException extends RuntimeException {
  public ServiceException(String msg, Throwable cause) {
      super(msg, cause);
  }
}
