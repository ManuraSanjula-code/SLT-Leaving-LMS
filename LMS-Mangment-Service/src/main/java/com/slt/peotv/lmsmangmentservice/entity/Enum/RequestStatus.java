package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum RequestStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired");

    private final String description;
    RequestStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}

