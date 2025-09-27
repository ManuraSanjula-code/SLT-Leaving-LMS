package com.slt.radio.rosterservice.model.enums;

public enum RosterType {
    CHARANA_TV("CHARANA TV"),
    NORMAL("NORAML"),
    NONE("NONE");

    private final String description;
    RosterType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
