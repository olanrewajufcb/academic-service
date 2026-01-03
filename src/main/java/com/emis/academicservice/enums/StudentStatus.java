package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StudentStatus {

    GRADUATED("Graduated"),
    TRANSFERRED("Transferred"),
    WITHDRAWN("Withdrawn"),
    SUSPENDED("Suspended"),
    ENROLLED("Enrolled"),
    DROPPED_OUT("Dropped Out"),
    ADMITTED("Admitted");

    StudentStatus(String value) {
        this.value = value;
    }

    private final String value;

    @JsonCreator
    public static StudentStatus fromValue(String value) {
        for (StudentStatus status : StudentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
