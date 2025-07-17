package com.slt.radio.rosterservice.Model.Enum;

public enum RosterType {
    CHARANA_TV("CHARANA TV"),
    NORMAL("NORAML");

    private final String description;
    RosterType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
