package com.slt.peotv.userservice.lms.shared.model.request;

import java.util.List;

public class ProfileReq {
    private String name;
    private String publicId;
    private String workStart;
    private String workEnds;
    private String ignoreSl;
    private String gracePeriodStart;
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
    private List<String> addedUsers;
    private List<String> deletedUsers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

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

    public String getGracePeriodStart() {
        return gracePeriodStart;
    }

    public void setGracePeriodStart(String gracePeriodStart) {
        this.gracePeriodStart = gracePeriodStart;
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

    public List<String> getAddedUsers() {
        return addedUsers;
    }

    public void setAddedUsers(List<String> addedUsers) {
        this.addedUsers = addedUsers;
    }

    public List<String> getDeletedUsers() {
        return deletedUsers;
    }

    public void setDeletedUsers(List<String> deletedUsers) {
        this.deletedUsers = deletedUsers;
    }
}
