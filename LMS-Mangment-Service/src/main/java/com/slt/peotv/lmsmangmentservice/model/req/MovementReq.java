package com.slt.peotv.lmsmangmentservice.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Date;

public class MovementReq {

    private String publicId;
    private Long id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    private String comment;

    @NotBlank(message = "Destination is required")
    private String destination;

    private String category;

    @NotNull(message = "Happen date is required")
    private Date happenDate;

    @NotNull(message = "Log time is required")
    private Date logTime;

    @NotBlank(message = "In time is required")
    private Time inTime;

    @NotBlank(message = "Out time is required")
    private Time outTime;

    @JsonProperty("inTimeRaw")
    private String inTimeRaw;

    @JsonProperty("outTimeRaw")
    private String outTimeRaw;

    @JsonProperty("happenDateRaw")
    private String happenDateRaw;


    private RequestStatus requestStatus = RequestStatus.DRAFT;

    private Date reqDate;
    private Integer attSync = 0;
    private Long attendanceId;
    private Date createDate;
    private Date updateDate;
    private Boolean isEdited = false;

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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public Time getInTime() {
        return inTime;
    }

    public void setInTime(Time inTime) {
        this.inTime = inTime;
    }

    public Time getOutTime() {
        return outTime;
    }

    public void setOutTime(Time outTime) {
        this.outTime = outTime;
    }

    public String getInTimeRaw() {
        return inTimeRaw;
    }

    public void setInTimeRaw(String inTimeRaw) {
        this.inTimeRaw = inTimeRaw;
    }

    public String getOutTimeRaw() {
        return outTimeRaw;
    }

    public void setOutTimeRaw(String outTimeRaw) {
        this.outTimeRaw = outTimeRaw;
    }

    public String getHappenDateRaw() {
        return happenDateRaw;
    }

    public void setHappenDateRaw(String happenDateRaw) {
        this.happenDateRaw = happenDateRaw;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Date getReqDate() {
        return reqDate;
    }

    public void setReqDate(Date reqDate) {
        this.reqDate = reqDate;
    }

    public Integer getAttSync() {
        return attSync;
    }

    public void setAttSync(Integer attSync) {
        this.attSync = attSync;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
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

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }
}