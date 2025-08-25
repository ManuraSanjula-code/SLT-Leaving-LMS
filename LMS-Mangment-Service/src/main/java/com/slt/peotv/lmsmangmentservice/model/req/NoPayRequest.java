package com.slt.peotv.lmsmangmentservice.model.req;

public class NoPayRequest {
    private final boolean isHalfDay;
    private final boolean unAuthorized;
    private final boolean isUnsuccessful;
    private final boolean isLate;
    private final boolean isLateCover;
    private final boolean isAbsent;

    public NoPayRequest(boolean isHalfDay, boolean unAuthorized, boolean isUnsuccessful,
                        boolean isLate, boolean isLateCover, boolean isAbsent) {
        this.isHalfDay = isHalfDay;
        this.unAuthorized = unAuthorized;
        this.isUnsuccessful = isUnsuccessful;
        this.isLate = isLate;
        this.isLateCover = isLateCover;
        this.isAbsent = isAbsent;
    }

    public boolean isHalfDay() {
        return isHalfDay;
    }

    public boolean isUnAuthorized() {
        return unAuthorized;
    }

    public boolean isUnsuccessful() {
        return isUnsuccessful;
    }

    public boolean isLate() {
        return isLate;
    }

    public boolean isLateCover() {
        return isLateCover;
    }

    public boolean isAbsent() {
        return isAbsent;
    }
}
