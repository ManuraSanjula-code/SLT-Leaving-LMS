package com.slt.peotv.lmsmangmentservice.model.dto;

import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import javax.persistence.Transient;

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
    private Long numOfDays;
    private String description;
    private ComponentBehavior componentBehavior;
    private RequestStatus requestStatus;
    private Boolean notUsed = false;
    private Boolean isManualRequest = false;
    private Date happenDate;
    private Date createDate;
    private Date updateDate;
    private String userId;
    private Boolean isEdited = false;

    @Transient
    private List<LeaveTra> adminsTra;

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

    public ComponentBehavior getComponentBehavior() {
        return componentBehavior;
    }

    public void setComponentBehavior(ComponentBehavior componentBehavior) {
        this.componentBehavior = componentBehavior;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Boolean getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(Boolean notUsed) {
        this.notUsed = notUsed;
    }

    public Boolean getIsManualRequest() {
        return isManualRequest;
    }

    public void setIsManualRequest(Boolean isManualRequest) {
        this.isManualRequest = isManualRequest;
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

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public List<LeaveTra> getAdminsTra() {
        return adminsTra;
    }

    public void setAdminsTra(List<LeaveTra> adminsTra) {
        this.adminsTra = adminsTra;
    }

    public Boolean getHalfDay() {
        return componentBehavior == ComponentBehavior.HALF_DAY;
    }


    public Boolean getFullDay() {
        return componentBehavior == ComponentBehavior.FULL_DAY;
    }


    public Boolean getUnSuccessful() {
        return componentBehavior == ComponentBehavior.UNSUCCESSFUL;
    }


    public Boolean getUnauthorized() {
        return componentBehavior == ComponentBehavior.UNAUTHORIZED;
    }


    public Boolean getLate() {
        return componentBehavior == ComponentBehavior.LATE;
    }


    public Boolean getLateCover() {
        return componentBehavior == ComponentBehavior.LATE_COVER;
    }


    public Boolean getShort_Leave() {
        return componentBehavior == ComponentBehavior.SHORT_LEAVE;
    }


    public Boolean getAbsent() {
        return componentBehavior == ComponentBehavior.ABSENT;
    }

    public Boolean getPending() {
        return requestStatus == RequestStatus.PENDING_APPROVAL || requestStatus == RequestStatus.SUBMITTED;
    }


    public Boolean getAccepted() {
        return requestStatus == RequestStatus.APPROVED;
    }


    public Boolean getCanceled() {
        return requestStatus == RequestStatus.CANCELLED;
    }

    public Boolean getReject() {
        return requestStatus == RequestStatus.REJECTED;
    }


    public String getLeaveStatusString() {
        if (requestStatus != null) {
            return requestStatus.getDescription();
        }
        return "Unknown";
    }


    public String getComponentBehaviorString() {
        if (componentBehavior != null) {
            return componentBehavior.getDisplayName();
        }
        return "Unknown";
    }

    public double getActualDays() {
        return this.numOfDays != null ? this.numOfDays / 2.0 : 0.0;
    }

    public boolean isSingleDayLeave() {
        return numOfDays != null && numOfDays == 2;
    }

    public boolean isHalfDayLeave() {
        return numOfDays != null && numOfDays == 1;
    }

    public boolean isMultiDayLeave() {
        return numOfDays != null && numOfDays > 2;
    }
}