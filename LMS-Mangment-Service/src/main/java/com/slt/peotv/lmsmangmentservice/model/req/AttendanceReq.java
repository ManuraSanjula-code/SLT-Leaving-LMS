package com.slt.peotv.lmsmangmentservice.model.req;

import java.sql.Time;
import java.util.Date;

public class AttendanceReq {
    private Date date;
    private String employeeID;
    private Boolean isFullDay = false;
    private Date arrivalDate;
    private Time arrivalTime;
    private Time leftTime;
    private Boolean isLate = false;
    private Boolean lateCover = false;
    private Boolean isHalfDay = false;
    private Boolean isFullLeave = false;
    private Boolean isShortLeave = false;
    private Boolean isAbsent = false;
    private Boolean isUnSuccessful = false;
    private Boolean isNoPay = false;
    private Boolean issues = false;
    private Boolean isUnAuthorized = false;
    private Boolean resolve = false;
    private Boolean leaveSuccess = false;
    private Boolean leaveReq = false;
    private String issueDescription;
    private Date dueDateForUA;
    private Boolean active = true;
    private Boolean nopay = false;
    private String terminalID;

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    private Boolean viaMovement = false;
    private Boolean viaLeave = false;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public Boolean getFullDay() {
        return isFullDay;
    }

    public void setFullDay(Boolean fullDay) {
        isFullDay = fullDay;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Time getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(Time leftTime) {
        this.leftTime = leftTime;
    }

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getLateCover() {
        return lateCover;
    }

    public void setLateCover(Boolean lateCover) {
        this.lateCover = lateCover;
    }

    public Boolean getHalfDay() {
        return isHalfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        isHalfDay = halfDay;
    }

    public Boolean getFullLeave() {
        return isFullLeave;
    }

    public void setFullLeave(Boolean fullLeave) {
        isFullLeave = fullLeave;
    }

    public Boolean getShortLeave() {
        return isShortLeave;
    }

    public void setShortLeave(Boolean shortLeave) {
        isShortLeave = shortLeave;
    }

    public Boolean getAbsent() {
        return isAbsent;
    }

    public void setAbsent(Boolean absent) {
        isAbsent = absent;
    }

    public Boolean getUnSuccessful() {
        return isUnSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        isUnSuccessful = unSuccessful;
    }

    public Boolean getNoPay() {
        return isNoPay;
    }

    public void setNoPay(Boolean noPay) {
        isNoPay = noPay;
    }

    public Boolean getIssues() {
        return issues;
    }

    public void setIssues(Boolean issues) {
        this.issues = issues;
    }

    public Boolean getUnAuthorized() {
        return isUnAuthorized;
    }

    public void setUnAuthorized(Boolean unAuthorized) {
        isUnAuthorized = unAuthorized;
    }

    public Boolean getResolve() {
        return resolve;
    }

    public void setResolve(Boolean resolve) {
        this.resolve = resolve;
    }

    public Boolean getLeaveSuccess() {
        return leaveSuccess;
    }

    public void setLeaveSuccess(Boolean leaveSuccess) {
        this.leaveSuccess = leaveSuccess;
    }

    public Boolean getLeaveReq() {
        return leaveReq;
    }

    public void setLeaveReq(Boolean leaveReq) {
        this.leaveReq = leaveReq;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getNopay() {
        return nopay;
    }

    public void setNopay(Boolean nopay) {
        this.nopay = nopay;
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
}
