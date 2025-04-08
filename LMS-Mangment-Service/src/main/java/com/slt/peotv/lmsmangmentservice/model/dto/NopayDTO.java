package com.slt.peotv.lmsmangmentservice.model.dto;

import java.util.Date;

public class NopayDTO {
    private Long id;
    public String publicId;
    private String employeeID;
    private Date submissionDate;
    private Date acctualDate;
    private Boolean isHalfDay = false;
    private Boolean unSuccessful = false;
    private Boolean isLate = false;
    private Boolean isLateCover = false;
    private Boolean isAbsent = false;
    private String attendance;
    private String comment;
    private Date happenDate;

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

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Date getAcctualDate() {
        return acctualDate;
    }

    public void setAcctualDate(Date acctualDate) {
        this.acctualDate = acctualDate;
    }

    public Boolean getHalfDay() {
        return isHalfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        isHalfDay = halfDay;
    }

    public Boolean getUnSuccessful() {
        return unSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        this.unSuccessful = unSuccessful;
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

    public Boolean getAbsent() {
        return isAbsent;
    }

    public void setAbsent(Boolean absent) {
        isAbsent = absent;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }
}
