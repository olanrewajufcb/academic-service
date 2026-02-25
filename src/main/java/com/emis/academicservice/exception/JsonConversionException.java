package com.emis.academicservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonConversionException extends RuntimeException {
  public JsonConversionException(String msg,
                                 Throwable cause)  {
    super(msg, cause);
  }
}
