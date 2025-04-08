package com.slt.peotv.lmsmangmentservice.model.dto;

import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import jakarta.persistence.*;

import java.util.Date;

public class MovementDTO {
    private Long id;
    private String publicId;
    private String employeeID;
    private String inTime;
    private String outTime;
    private String comment;
    private Date logTime;
    private Date supAppTime;
    private Date manAppTime;
    private Date hodAppTime;
    private String category;
    private String destination;
    private String employeeId;
    private Date reqDate;
    private String hod;
    private String supervisor;
    @Enumerated(EnumType.STRING)  // FIXED: Enum mapping
    private MovementType movementType;
    private Integer attSync = 0;
    private Date happenDate;
    private Boolean isPending = false;
    private Boolean isAccepted = false;
    private Boolean isExpired = false;
    private Boolean isHalfDay = false;
    private Boolean isFullDay = false;
    private Boolean isLate = false;
    private Boolean isAbsent = false;
    private Boolean isUnSuccessfulAttdate = false;
    private Boolean isLateCover = false;
    private Boolean unAuthorized = false;
    private String attendance;

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

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
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

    public Date getSupAppTime() {
        return supAppTime;
    }

    public void setSupAppTime(Date supAppTime) {
        this.supAppTime = supAppTime;
    }

    public Date getManAppTime() {
        return manAppTime;
    }

    public void setManAppTime(Date manAppTime) {
        this.manAppTime = manAppTime;
    }

    public Date getHodAppTime() {
        return hodAppTime;
    }

    public void setHodAppTime(Date hodAppTime) {
        this.hodAppTime = hodAppTime;
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

    public String getHod() {
        return hod;
    }

    public void setHod(String hod) {
        this.hod = hod;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
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

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
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

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getAbsent() {
        return isAbsent;
    }

    public void setAbsent(Boolean absent) {
        isAbsent = absent;
    }

    public Boolean getUnSuccessfulAttdate() {
        return isUnSuccessfulAttdate;
    }

    public void setUnSuccessfulAttdate(Boolean unSuccessfulAttdate) {
        isUnSuccessfulAttdate = unSuccessfulAttdate;
    }

    public Boolean getLateCover() {
        return isLateCover;
    }

    public void setLateCover(Boolean lateCover) {
        isLateCover = lateCover;
    }

    public Boolean getUnAuthorized() {
        return unAuthorized;
    }

    public void setUnAuthorized(Boolean unAuthorized) {
        this.unAuthorized = unAuthorized;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }
}
