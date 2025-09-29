package com.slt.radio.rosterservice.documents.enums;

public enum AttendanceType {
    FULL_DAY("Full Day Attendance"),
    HALF_DAY("Half Day Attendance"),
    NONE("NONE"),
    ABSENT("Absent");

    private final String description;
    AttendanceType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
