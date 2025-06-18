package com.slt.radio.rosterservice.Model.Enum;

public enum AttendanceType {
    FULL_DAY("Full Day Attendance"),
    HALF_DAY("Half Day Attendance"),
    ABSENT("Absent");

    private final String description;
    AttendanceType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
