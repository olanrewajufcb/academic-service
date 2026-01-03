package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GradeLevel {
    PRE_NURSERY("PRE_NURSERY"),
    NURSERY_1("NURSERY_1"),
    NURSERY_2("NURSERY_2"),
    PRIMARY_1("PRIMARY_1"),
    PRIMARY_2("PRIMARY_2"),
    PRIMARY_3("PRIMARY_3"),
    PRIMARY_4("PRIMARY_4"),
    PRIMARY_5("PRIMARY_5"),
    PRIMARY_6("PRIMARY_6"),
    JSS_1("JSS_1"),
    JSS_2("JSS_2"),
    JSS_3("JSS_3"),
    SS_1("SS_1"),
    SS_2("SS_2"),
    SS_3("SS_3");

    GradeLevel(String value) {
        this.value = value;
    }

    private final String value;

    @JsonCreator
    public static GradeLevel fromString(String gradeLevel) {
        for (GradeLevel level : GradeLevel.values()) {
            if (level.name().equalsIgnoreCase(gradeLevel)) {
                return level;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return  this.name();
    }

}
