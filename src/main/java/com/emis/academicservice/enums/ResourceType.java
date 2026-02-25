package com.emis.academicservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ResourceType {

    PDF,
    VIDEO,
    PRESENTATION,
    DOCUMENT,
    IMAGE,
    AUDIO,
    OTHER;

    @JsonCreator
    public static ResourceType fromString(String value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
