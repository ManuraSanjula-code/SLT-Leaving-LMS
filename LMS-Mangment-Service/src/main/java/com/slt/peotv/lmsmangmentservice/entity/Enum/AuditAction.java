package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum AuditAction {
    CREATE("Created"),
    UPDATE("Updated"),
    DELETE("Deleted"),
    APPROVE("Approved"),
    REJECT("Rejected"),
    RESOLVE("Resolved");

    private final String description;
    AuditAction(String description) { this.description = description; }
    public String getDescription() { return description; }
}
