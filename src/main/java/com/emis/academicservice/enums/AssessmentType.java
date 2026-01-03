package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssessmentType {
    EXAM("EXAM"),
    TEST("TEST"),
    ASSIGNMENT("ASSIGNMENT"),
    PRACTICAL("PRACTICAL");

    AssessmentType(String value) {
        this.value = value;
    }

    private final String value;

    @JsonCreator
    public static AssessmentType fromValue(String value) {
        for (AssessmentType type : AssessmentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}