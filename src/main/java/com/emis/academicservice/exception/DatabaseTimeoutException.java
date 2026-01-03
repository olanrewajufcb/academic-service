package com.emis.academicservice.exception;

public class DatabaseTimeoutException extends Throwable {
  public DatabaseTimeoutException(String s, Throwable error) {
    super(s, error);
  }
}
