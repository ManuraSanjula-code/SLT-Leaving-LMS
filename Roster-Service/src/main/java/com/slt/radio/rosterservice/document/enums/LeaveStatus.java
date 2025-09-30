package com.slt.radio.rosterservice.document.enums;

public enum LeaveStatus {
    NO_LEAVE("No Leave"),
    FULL_LEAVE("Full Day Leave"),
    SHORT_LEAVE("Short Leave"),
    LEAVE_REQUESTED("Leave Requested"),
    LEAVE_APPROVED("Leave Approved");

    private final String description;
    LeaveStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}
