package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    EXCUSED("Excused"),
    SICK_LEAVE("Sick Leave");

    AttendanceStatus(String value) {
        this.value = value;
    }


    private final String value;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static AttendanceStatus fromValue(String value) {
        for (AttendanceStatus type : AttendanceStatus.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}