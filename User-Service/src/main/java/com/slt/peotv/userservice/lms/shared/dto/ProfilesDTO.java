package com.slt.peotv.userservice.lms.shared.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public class ProfilesDTO implements Serializable {
    private static final long serialVersionUID = 4466760523447920000L;
   
    private Integer id;
    private String name;
    public String publicId;
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
    private Collection<UserDto> users;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Collection<UserDto> getUsers() {
        return users;
    }

    public void setUsers(Collection<UserDto> users) {
        this.users = users;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
			return false;
		}
        ProfilesDTO that = (ProfilesDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(publicId, that.publicId) && Objects.equals(workStart, that.workStart) && Objects.equals(workEnds, that.workEnds) && Objects.equals(ignoreSl, that.ignoreSl) && Objects.equals(gracePeriodeStart, that.gracePeriodeStart) && Objects.equals(hdStart, that.hdStart) && Objects.equals(slStartMorning, that.slStartMorning) && Objects.equals(slStartEvening, that.slStartEvening) && Objects.equals(possibleFpLocations, that.possibleFpLocations) && Objects.equals(defaultHrs, that.defaultHrs) && Objects.equals(hdHrs, that.hdHrs) && Objects.equals(minHrsForSl, that.minHrsForSl) && Objects.equals(shortLeaveCount, that.shortLeaveCount) && Objects.equals(hdEndsMorning, that.hdEndsMorning) && Objects.equals(flexiDays, that.flexiDays) && Objects.equals(flexiHrsStart, that.flexiHrsStart) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, publicId, workStart, workEnds, ignoreSl, gracePeriodeStart, hdStart, slStartMorning, slStartEvening, possibleFpLocations, defaultHrs, hdHrs, minHrsForSl, shortLeaveCount, hdEndsMorning, flexiDays, flexiHrsStart, users);
    }
}
