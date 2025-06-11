package com.slt.peotv.lmsmangmentservice.entity.Enum;

public enum ResolveType {
    VIA_MOVEMENT("VIA MOVEMENT"),
    VIA_LEAVE("VIA LEAVE"),
    EXPIRED("EXPIRED");
    private final String description;
    ResolveType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
