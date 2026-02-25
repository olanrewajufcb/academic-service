package com.emis.academicservice.exception;

public class ResourceCreationException extends RuntimeException{
  public ResourceCreationException(String msg, Throwable error) {
    super(msg, error);
  }
}
