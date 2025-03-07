package com.slt.peotv.userservice.lms.entity.company;

import com.slt.peotv.userservice.lms.entity.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name="profile")
@Setter
@Getter
@EqualsAndHashCode
public class ProfilesEntity implements Serializable {
    private static final long serialVersionUID = 4466760523447920000L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "Name", length = 45, nullable = false)
    private String name;

    @Column(nullable = false)
    public String publicId;

    @Column(name = "work_start", length = 45, nullable = false)
    private String workStart;

    @Column(name = "work_ends", length = 45, nullable = false)
    private String workEnds;

    @Column(name = "ignore_sl", length = 45, nullable = false)
    private String ignoreSl;

    @Column(name = "grace_periode_start", length = 45, nullable = false)
    private String gracePeriodeStart;

    @Column(name = "hd_start", length = 45, nullable = false)
    private String hdStart;

    @Column(name = "sl_start_morning", length = 45, nullable = false)
    private String slStartMorning;

    @Column(name = "sl_start_evening", length = 45, nullable = false)
    private String slStartEvening;

    @Column(name = "possible_fp_locations", columnDefinition = "TEXT", nullable = false)
    private String possibleFpLocations;

    @Column(name = "default_hrs", length = 45, nullable = false)
    private String defaultHrs;

    @Column(name = "hd_hrs", length = 45, nullable = false)
    private String hdHrs;

    @Column(name = "min_hrs_for_sl", length = 45, nullable = false)
    private String minHrsForSl;

    @Column(name = "short_leave_count", length = 45, nullable = false)
    private String shortLeaveCount;

    @Column(name = "hd_ends_morning", length = 45, nullable = false)
    private String hdEndsMorning;

    @Column(name = "flexi_days", length = 45, nullable = false)
    private String flexiDays;

    @Column(name = "flexi_hrs_start", length = 45, nullable = false)
    private String flexiHrsStart;

    @ManyToMany(mappedBy = "profiles")
    private Collection<UserEntity> users;

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

    public Collection<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Collection<UserEntity> users) {
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
        if (o == null || getClass() != o.getClass()) return false;
        ProfilesEntity that = (ProfilesEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(publicId, that.publicId) && Objects.equals(workStart, that.workStart) && Objects.equals(workEnds, that.workEnds) && Objects.equals(ignoreSl, that.ignoreSl) && Objects.equals(gracePeriodeStart, that.gracePeriodeStart) && Objects.equals(hdStart, that.hdStart) && Objects.equals(slStartMorning, that.slStartMorning) && Objects.equals(slStartEvening, that.slStartEvening) && Objects.equals(possibleFpLocations, that.possibleFpLocations) && Objects.equals(defaultHrs, that.defaultHrs) && Objects.equals(hdHrs, that.hdHrs) && Objects.equals(minHrsForSl, that.minHrsForSl) && Objects.equals(shortLeaveCount, that.shortLeaveCount) && Objects.equals(hdEndsMorning, that.hdEndsMorning) && Objects.equals(flexiDays, that.flexiDays) && Objects.equals(flexiHrsStart, that.flexiHrsStart) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, publicId, workStart, workEnds, ignoreSl, gracePeriodeStart, hdStart, slStartMorning, slStartEvening, possibleFpLocations, defaultHrs, hdHrs, minHrsForSl, shortLeaveCount, hdEndsMorning, flexiDays, flexiHrsStart, users);
    }
}
