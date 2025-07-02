package com.slt.peotv.lmsmangmentservice.messaging;

import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import java.time.LocalTime;
import java.util.Date;

public class Attendance {
    private String id;
    private String publicId;
    private Date date;
    private Date arrivalDate;
    private LocalTime arrivalTime;
    private LocalTime leftTime;
    private String terminalId;
    private String employeeId;
    private String teamId;
    private AttendanceType attendanceType;
    private Boolean isLate = false;
    private Boolean isLateCovered = false;
    private Boolean isUnauthorized = false;
    private Boolean isUnSuccessful = false;
    private Boolean isHoliday = false;
    private Boolean isResolved = false;
    private Boolean hasIssues = false;
    private Boolean isManual = false;
    private String issueDescription;
    private Date dueDateForUA;
    private Date etlRunTime = new Date();
    private Date createdDate = new Date();
    private Date updatedDate = new Date();
    private Boolean isActive = true;
    private Boolean viaMovement;
    private Boolean viaLeave;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(LocalTime leftTime) {
        this.leftTime = leftTime;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public AttendanceType getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(AttendanceType attendanceType) {
        this.attendanceType = attendanceType;
    }

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getLateCovered() {
        return isLateCovered;
    }

    public void setLateCovered(Boolean lateCovered) {
        isLateCovered = lateCovered;
    }

    public Boolean getUnauthorized() {
        return isUnauthorized;
    }

    public void setUnauthorized(Boolean unauthorized) {
        isUnauthorized = unauthorized;
    }

    public Boolean getUnSuccessful() {
        return isUnSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        isUnSuccessful = unSuccessful;
    }

    public Boolean getHoliday() {
        return isHoliday;
    }

    public void setHoliday(Boolean holiday) {
        isHoliday = holiday;
    }

    public Boolean getResolved() {
        return isResolved;
    }

    public void setResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public Date getDueDateForUA() {
        return dueDateForUA;
    }

    public void setDueDateForUA(Date dueDateForUA) {
        this.dueDateForUA = dueDateForUA;
    }

    public Date getEtlRunTime() {
        return etlRunTime;
    }

    public void setEtlRunTime(Date etlRunTime) {
        this.etlRunTime = etlRunTime;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getViaMovement() {
        return viaMovement;
    }

    public void setViaMovement(Boolean viaMovement) {
        this.viaMovement = viaMovement;
    }

    public Boolean getViaLeave() {
        return viaLeave;
    }

    public void setViaLeave(Boolean viaLeave) {
        this.viaLeave = viaLeave;
    }
    @Override
    public String toString() {
        return "Attendance{" +
                "id='" + id + '\'' +
                ", publicId='" + publicId + '\'' +
                ", date=" + date +
                ", arrivalDate=" + arrivalDate +
                ", arrivalTime=" + arrivalTime +
                ", leftTime=" + leftTime +
                ", terminalId='" + terminalId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", attendanceType=" + attendanceType +
                ", isLate=" + isLate +
                ", isLateCovered=" + isLateCovered +
                ", isUnauthorized=" + isUnauthorized +
                ", isUnSuccessful=" + isUnSuccessful +
                ", isHoliday=" + isHoliday +
                ", isResolved=" + isResolved +
                ", hasIssues=" + hasIssues +
                ", isManual=" + isManual +
                ", issueDescription='" + issueDescription + '\'' +
                ", dueDateForUA=" + dueDateForUA +
                ", etlRunTime=" + etlRunTime +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", isActive=" + isActive +
                ", viaMovement=" + viaMovement +
                ", viaLeave=" + viaLeave +
                '}';
    }
}
