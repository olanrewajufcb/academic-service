package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SchoolStage {

    PRE_NURSERY,
    NURSERY,
    PRIMARY,
    JUNIOR_SECONDARY,
    SENIOR_SECONDARY;

    @JsonCreator
    public static SchoolStage fromValue(String value) {
        for (SchoolStage stage : SchoolStage.values()) {
            if (stage.name().equalsIgnoreCase(value)) {
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
