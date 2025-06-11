package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum ComponentBehavior {
    HALF_DAY("Half Day"),
    FULL_DAY("Full Day"),
    UNSUCCESSFUL("Unsuccessful"),
    UNAUTHORIZED("Unauthorized"),
    LATE("Late"),
    LATE_COVER("Late Cover"),
    SHORT_LEAVE("Short Leave"),
    ABSENT("Absent");

    private final String displayName;

    ComponentBehavior(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
