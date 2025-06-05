package com.slt.peotv.lmsmangmentservice.model.dto;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public class AttendanceDTO {
    private Long id;
    private String publicId;
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
    private Boolean isManual = false;
    private String userId;
    private String terminalID;
    private List<InOutDTO> inOutDTOs;
    private List<EditedByDTO> editedByDTOs;

    public List<EditedByDTO> getEditedByDTOs() {
        return editedByDTOs;
    }

    public void setEditedByDTOs(List<EditedByDTO> editedByDTOs) {
        this.editedByDTOs = editedByDTOs;
    }

    public List<InOutDTO> getInOutDTOs() {
        return inOutDTOs;
    }

    public void setInOutDTOs(List<InOutDTO> inOutDTOs) {
        this.inOutDTOs = inOutDTOs;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Override
    public String toString() {
        return "AttendanceDTO{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", date=" + date +
                ", employeeID='" + employeeID + '\'' +
                ", isFullDay=" + isFullDay +
                ", arrivalDate=" + arrivalDate +
                ", arrivalTime=" + arrivalTime +
                ", leftTime=" + leftTime +
                ", isLate=" + isLate +
                ", lateCover=" + lateCover +
                ", isHalfDay=" + isHalfDay +
                ", isFullLeave=" + isFullLeave +
                ", isShortLeave=" + isShortLeave +
                ", isAbsent=" + isAbsent +
                ", isUnSuccessful=" + isUnSuccessful +
                ", isNoPay=" + isNoPay +
                ", issues=" + issues +
                ", isUnAuthorized=" + isUnAuthorized +
                ", resolve=" + resolve +
                ", leaveSuccess=" + leaveSuccess +
                ", leaveReq=" + leaveReq +
                ", issueDescription='" + issueDescription + '\'' +
                ", dueDateForUA=" + dueDateForUA +
                ", active=" + active +
                ", nopay=" + nopay +
                ", isManual=" + isManual +
                ", userId='" + userId + '\'' +
                '}';
    }
}
