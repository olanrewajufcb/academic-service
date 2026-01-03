package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AssessmentStatus {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    CANCELED("CANCELED"),
    REJECTED("REJECTED"),
    APPROVED("APPROVED"),
    SUBMITTED("SUBMITTED");

    AssessmentStatus(String value) {
        this.value = value;
    }

    private final String value;
    @JsonCreator
    public static AssessmentStatus fromString(String value) {
        for (AssessmentStatus status : AssessmentStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
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
