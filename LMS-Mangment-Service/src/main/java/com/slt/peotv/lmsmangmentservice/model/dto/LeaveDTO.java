package com.slt.peotv.lmsmangmentservice.model.dto;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import jakarta.persistence.Transient;

import java.util.Date;
import java.util.List;

public class LeaveDTO {
    private String publicId;
    private Long id;
    private String employeeID;
    private Date submitDate;
    private Date fromDate;
    private Date toDate;
    private LeaveTypeEntity leaveType;
    private Integer isNoPay = 0;
    private Long numOfDays;
    private String description;
    private Boolean isHalfDay;
    private Boolean isFullDay = false;
    private Boolean unSuccessful = false;
    private Boolean isUnauthorized = false;
    private Boolean isLate = false;
    private Boolean isLateCover = false;
    private Boolean isShort_Leave = false;
    private Boolean isPending = false;
    private Boolean isAccepted = false;
    private Boolean isAbsent = false;
    private Boolean notUsed = false;
    private Boolean isCanceled = false;
    private Boolean isManualRequest = false;
    private Boolean reject = false;
    private Boolean isEdited = false;
    private Date happenDate;
    private Date createDate;
    private Date updateDate;
    private String userId;

    @Transient
    private List<LeaveTra> adminsTra;
    private List<EditedByDTO> editedByDTOs;

    // Getters and Setters
    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public LeaveTypeEntity getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveTypeEntity leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getIsNoPay() {
        return isNoPay;
    }

    public void setIsNoPay(Integer isNoPay) {
        this.isNoPay = isNoPay;
    }

    public Long getNumOfDays() {
        return numOfDays;
    }

    public void setNumOfDays(Long numOfDays) {
        this.numOfDays = numOfDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHalfDay() {
        return isHalfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        isHalfDay = halfDay;
    }

    public Boolean getFullDay() {
        return isFullDay;
    }

    public void setFullDay(Boolean fullDay) {
        isFullDay = fullDay;
    }

    public Boolean getUnSuccessful() {
        return unSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        this.unSuccessful = unSuccessful;
    }

    public Boolean getUnauthorized() {
        return isUnauthorized;
    }

    public void setUnauthorized(Boolean unauthorized) {
        isUnauthorized = unauthorized;
    }

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getLateCover() {
        return isLateCover;
    }

    public void setLateCover(Boolean lateCover) {
        isLateCover = lateCover;
    }

    public Boolean getShort_Leave() {
        return isShort_Leave;
    }

    public void setShort_Leave(Boolean short_Leave) {
        isShort_Leave = short_Leave;
    }

    public Boolean getPending() {
        return isPending;
    }

    public void setPending(Boolean pending) {
        isPending = pending;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Boolean getAbsent() {
        return isAbsent;
    }

    public void setAbsent(Boolean absent) {
        isAbsent = absent;
    }

    public Boolean getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(Boolean notUsed) {
        this.notUsed = notUsed;
    }

    public Boolean getCanceled() {
        return isCanceled;
    }

    public void setCanceled(Boolean canceled) {
        isCanceled = canceled;
    }

    public Boolean getManualRequest() {
        return isManualRequest;
    }

    public void setManualRequest(Boolean manualRequest) {
        isManualRequest = manualRequest;
    }

    public Boolean getReject() {
        return reject;
    }

    public void setReject(Boolean reject) {
        this.reject = reject;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<LeaveTra> getAdminsTra() {
        return adminsTra;
    }

    public void setAdminsTra(List<LeaveTra> adminsTra) {
        this.adminsTra = adminsTra;
    }

    public List<EditedByDTO> getEditedByDTOs() {
        return editedByDTOs;
    }

    public void setEditedByDTOs(List<EditedByDTO> editedByDTOs) {
        this.editedByDTOs = editedByDTOs;
    }
}