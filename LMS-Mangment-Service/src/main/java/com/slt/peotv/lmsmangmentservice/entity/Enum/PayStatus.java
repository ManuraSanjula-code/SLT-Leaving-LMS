package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum PayStatus {
    NO_PAY("No Pay");

    private final String description;
    PayStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}
