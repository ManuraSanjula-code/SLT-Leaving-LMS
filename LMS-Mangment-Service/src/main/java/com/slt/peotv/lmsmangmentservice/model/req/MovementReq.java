package com.slt.peotv.lmsmangmentservice.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
public class MovementReq {
    private String employeeId;
    private String userId;
    private MovementType movementType;
    private String comment;
    private String destination;
    private String category;
    private Date happenDate;
    private Boolean isAbsent;
    private Boolean isUnSuccessfulAttdate;
    private Boolean isHalfDay;
    private Boolean unAuthorized;
    private Boolean isLate = false;
    private Boolean isLateCover = false;
    private Date logTime;
    private String intime;
    private String outtime;
    private String adminId;
    private String adminComment;

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

    public Boolean getHalfDay() {
        return isHalfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        isHalfDay = halfDay;
    }

    public Boolean getUnAuthorized() {
        return unAuthorized;
    }

    public void setUnAuthorized(Boolean unAuthorized) {
        this.unAuthorized = unAuthorized;
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

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public String getOuttime() {
        return outtime;
    }

    public void setOuttime(String outtime) {
        this.outtime = outtime;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}
