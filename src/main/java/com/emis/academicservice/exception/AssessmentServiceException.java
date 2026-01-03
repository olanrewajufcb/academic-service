package com.emis.academicservice.exception;

import org.springframework.dao.DataAccessException;

public class AssessmentServiceException extends  RuntimeException {
  public AssessmentServiceException(String msg, DataAccessException ex) {
    super(msg, ex);
  }

  public AssessmentServiceException(String databaseErrorOccurred, Throwable ex) {
    super(databaseErrorOccurred, ex);
  }
}
