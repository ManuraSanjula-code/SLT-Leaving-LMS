package com.slt.peotv.lmsmangmentservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovementDTO {
    private Long id;
    private String publicId;
    private String userId;
    private String inTime;
    private String outTime;
    private String comment;
    private Date logTime;
    private String category;
    private String destination;
    private String employeeId;
    private Date reqDate;

    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    private Integer attSync = 0;
    private Date happenDate;
    private RequestStatus requestStatus;
    private Date createDate;
    private Date updateDate;
    private Boolean isEdited = false;

    private String attendance;

    @JsonIgnore
    private List<MovementAdminsDTO> movementAdmins = new ArrayList<>();

    @Transient
    private List<MovementTra> adminsTra;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Date getReqDate() {
        return reqDate;
    }

    public void setReqDate(Date reqDate) {
        this.reqDate = reqDate;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getAttSync() {
        return attSync;
    }

    public void setAttSync(Integer attSync) {
        this.attSync = attSync;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
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

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public List<MovementAdminsDTO> getMovementAdmins() {
        return movementAdmins;
    }

    public void setMovementAdmins(List<MovementAdminsDTO> movementAdmins) {
        this.movementAdmins = movementAdmins;
    }

    public List<MovementTra> getAdminsTra() {
        return adminsTra;
    }

    public void setAdminsTra(List<MovementTra> adminsTra) {
        this.adminsTra = adminsTra;
    }

    public Boolean getPending() {
        return requestStatus == RequestStatus.PENDING_APPROVAL || requestStatus == RequestStatus.SUBMITTED;
    }

    public void setPending(Boolean pending) {
        if (Boolean.TRUE.equals(pending)) {
            this.requestStatus = RequestStatus.PENDING_APPROVAL;
        }
    }


    public Boolean getAccepted() {
        return requestStatus == RequestStatus.APPROVED;
    }


    public void setAccepted(Boolean accepted) {
        if (Boolean.TRUE.equals(accepted)) {
            this.requestStatus = RequestStatus.APPROVED;
        }
    }


    public Boolean getReject() {
        return requestStatus == RequestStatus.REJECTED;
    }


    public void setReject(Boolean reject) {
        if (Boolean.TRUE.equals(reject)) {
            this.requestStatus = RequestStatus.REJECTED;
        }
    }

    public int getMovementDurationMinutes() {
        if (this.inTime != null && this.outTime != null) {
            try {
                int inMinutes = timeToMinutes(this.inTime);
                int outMinutes = timeToMinutes(this.outTime);

                if (inMinutes != -1 && outMinutes != -1) {
                    return Math.max(0, outMinutes - inMinutes);
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }


    private int timeToMinutes(String time) {
        if (time == null || time.trim().isEmpty()) {
            return -1;
        }

        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return -1;
            }
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean isSameDayMovement() {
        if (this.inTime != null && this.outTime != null) {
            int inMinutes = timeToMinutes(this.inTime);
            int outMinutes = timeToMinutes(this.outTime);
            return outMinutes > inMinutes;
        }
        return true;
    }


    public boolean canBeEdited() {
        return this.requestStatus != RequestStatus.APPROVED &&
                this.requestStatus != RequestStatus.REJECTED &&
                this.requestStatus != RequestStatus.CANCELLED &&
                this.requestStatus != RequestStatus.EXPIRED;
    }


    public boolean canBeDeleted() {
        return this.requestStatus == RequestStatus.DRAFT ||
                this.requestStatus == RequestStatus.SUBMITTED;
    }
}