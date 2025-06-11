package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum NoPayReason {
    HALF_DAY("Half Day Attendance"),
    UNSUCCESSFUL("Unsuccessful Day"),
    LATE("Late Arrival"),
    LATE_NOT_COVERED("Late Arrival Not Covered"),
    ABSENT("Absent from Work"),
    UNAUTHORIZED("Unauthorized Absence");

    private final String description;
    NoPayReason(String description) { this.description = description; }
    public String getDescription() { return description; }
    public static NoPayReason fromDescription(String description) {
        if (description == null) {
            return null;
        }

        for (NoPayReason reason : NoPayReason.values()) {
            if (reason.getDescription().equals(description)) {
                return reason;
            }
        }
        return null;
    }
}
