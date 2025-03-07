package com.slt.peotv.userservice.lms.shared.model.request;


public class ProfileReq {
    private String workStart;
    private String workEnds;
    private String ignoreSl;
    private String gracePeriodeStart;
    private String hdStart;
    private String slStartMorning;
    private String slStartEvening;
    private String possibleFpLocations;
    private String defaultHrs;
    private String hdHrs;
    private String minHrsForSl;
    private String shortLeaveCount;
    private String hdEndsMorning;
    private String flexiDays;
    private String flexiHrsStart;

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnds() {
        return workEnds;
    }

    public void setWorkEnds(String workEnds) {
        this.workEnds = workEnds;
    }

    public String getIgnoreSl() {
        return ignoreSl;
    }

    public void setIgnoreSl(String ignoreSl) {
        this.ignoreSl = ignoreSl;
    }

    public String getGracePeriodeStart() {
        return gracePeriodeStart;
    }

    public void setGracePeriodeStart(String gracePeriodeStart) {
        this.gracePeriodeStart = gracePeriodeStart;
    }

    public String getHdStart() {
        return hdStart;
    }

    public void setHdStart(String hdStart) {
        this.hdStart = hdStart;
    }

    public String getSlStartMorning() {
        return slStartMorning;
    }

    public void setSlStartMorning(String slStartMorning) {
        this.slStartMorning = slStartMorning;
    }

    public String getSlStartEvening() {
        return slStartEvening;
    }

    public void setSlStartEvening(String slStartEvening) {
        this.slStartEvening = slStartEvening;
    }

    public String getPossibleFpLocations() {
        return possibleFpLocations;
    }

    public void setPossibleFpLocations(String possibleFpLocations) {
        this.possibleFpLocations = possibleFpLocations;
    }

    public String getDefaultHrs() {
        return defaultHrs;
    }

    public void setDefaultHrs(String defaultHrs) {
        this.defaultHrs = defaultHrs;
    }

    public String getHdHrs() {
        return hdHrs;
    }

    public void setHdHrs(String hdHrs) {
        this.hdHrs = hdHrs;
    }

    public String getMinHrsForSl() {
        return minHrsForSl;
    }

    public void setMinHrsForSl(String minHrsForSl) {
        this.minHrsForSl = minHrsForSl;
    }

    public String getShortLeaveCount() {
        return shortLeaveCount;
    }

    public void setShortLeaveCount(String shortLeaveCount) {
        this.shortLeaveCount = shortLeaveCount;
    }

    public String getHdEndsMorning() {
        return hdEndsMorning;
    }

    public void setHdEndsMorning(String hdEndsMorning) {
        this.hdEndsMorning = hdEndsMorning;
    }

    public String getFlexiDays() {
        return flexiDays;
    }

    public void setFlexiDays(String flexiDays) {
        this.flexiDays = flexiDays;
    }

    public String getFlexiHrsStart() {
        return flexiHrsStart;
    }

    public void setFlexiHrsStart(String flexiHrsStart) {
        this.flexiHrsStart = flexiHrsStart;
    }
}
