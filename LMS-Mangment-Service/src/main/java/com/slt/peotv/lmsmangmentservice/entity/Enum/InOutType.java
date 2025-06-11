package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum InOutType {
    MORNING_IN("Morning Punch In"),
    MORNING_OUT("Morning Punch Out"),
    EVENING_IN("Evening Punch In"),
    EVENING_OUT("Evening Punch Out"),
    SINGLE_PUNCH("Single Punch");

    private final String description;
    InOutType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
