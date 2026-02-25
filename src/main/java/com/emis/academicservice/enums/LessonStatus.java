package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LessonStatus {

    SCHEDULED,
    COMPLETED,
    CANCELED;

    @JsonCreator
    public static LessonStatus fromString(String status) {
        for (LessonStatus value : LessonStatus.values()) {
            if (value.name().equalsIgnoreCase(status)) {
                return value;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
