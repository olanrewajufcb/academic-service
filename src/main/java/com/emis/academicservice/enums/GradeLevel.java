package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GradeLevel {
    PRE_NURSERY,
    NURSERY,
    PRIMARY_ONE,
    PRIMARY_TWO,
    PRIMARY_THREE,
    PRIMARY_FOUR,
    PRIMARY_FIVE,
    PRIMARY_SIX,
    JSS_ONE,
    JSS_TWO,
    JSS_THREE,
    SS_ONE,
    SS_TWO,
    SS_THREE,
    GRADUATE;

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
