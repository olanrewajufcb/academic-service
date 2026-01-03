package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchoolStage {

    PRE_NURSERY("Pre-Nursery"),
    NURSERY("Nursery"),
    PRIMARY("Primary"),
    JUNIOR_SECONDARY("Junior Secondary"),
    SENIOR_SECONDARY("Senior Secondary");

    SchoolStage(String value) {
        this.value = value;
    }

    private final String value;

    @JsonCreator
    public static SchoolStage fromValue(String value) {
        for (SchoolStage stage : SchoolStage.values()) {
            if (stage.value.equalsIgnoreCase(value)) {
                return stage;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}
