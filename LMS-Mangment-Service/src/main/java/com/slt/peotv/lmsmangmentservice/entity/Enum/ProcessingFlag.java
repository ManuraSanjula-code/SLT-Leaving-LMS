package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum ProcessingFlag {
    MANUAL("Manual Entry"),
    AUTOMATIC("Automatic"),
    EDITED("Manually Edited"),
    RESOLVED("Issue Resolved"),
    VIA_MOVEMENT("Via Movement"),
    VIA_LEAVE("Via Leave");

    private final String description;
    ProcessingFlag(String description) { this.description = description; }
    public String getDescription() { return description; }
}
