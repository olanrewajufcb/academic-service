package com.emis.academicservice.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException{

  private final String fieldName;
  private final Object rejectedValue;

  public ResourceAlreadyExistsException(String msg, String fieldName, Object rejectedValue) {
    super(msg);
    this.fieldName = fieldName;
    this.rejectedValue = rejectedValue;
  }
  public ResourceAlreadyExistsException(String msg) {
    super(msg);
    this.fieldName = null;
    this.rejectedValue = null;
  }

}
