package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EnrollmentStatus {
    ENROLLED("Enrolled"),
    ADMITTED("Admitted"),
    DROPPED("Dropped"),
    REJECTED("Rejected");
    EnrollmentStatus(String value) {
        this.value = value;
    }

    private final String value;
    @JsonCreator
    public static EnrollmentStatus fromString(String status) {
        for (EnrollmentStatus value : EnrollmentStatus.values()) {
            if (value.name().equalsIgnoreCase(status)) {
                return value;
            }
        }
        return null;
    }
    @JsonValue
    public String toValue() {
        return this.name();
    }


}