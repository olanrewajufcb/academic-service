package com.emis.academicservice.exception;

public class SchoolCapacityExceededException extends  RuntimeException {
  public SchoolCapacityExceededException(Long schoolId, long currentCount, long maxCapacity) {
      super("School maximum capacity exceeded for " + schoolId + " (" + currentCount + "/" + maxCapacity + ")");
  }
}
