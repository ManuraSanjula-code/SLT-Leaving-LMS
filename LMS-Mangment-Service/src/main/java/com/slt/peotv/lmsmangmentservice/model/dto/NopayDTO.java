package com.slt.peotv.lmsmangmentservice.model.dto;

import java.util.Date;
import java.util.List;

public class NopayDTO {
    private Long id;
    private String publicId;
    private String employeeId;
    private Long attendanceId;
    private Date submissionDate;
    private Date date;
    private String comment;
    private Date createdDate;
    private Date updatedDate;
    private Boolean isActive = true;
    private NoPayReasonDTO reasons;

    public NopayDTO() {}

    public NopayDTO(Long id, String publicId, String employeeId, Date submissionDate, Date date) {
        this.id = id;
        this.publicId = publicId;
        this.employeeId = employeeId;
        this.submissionDate = submissionDate;
        this.date = date;
    }

    // Getters and Setters
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public NoPayReasonDTO getReasons() {
        return reasons;
    }

    public void setReasons(NoPayReasonDTO reasons) {
        this.reasons = reasons;
    }

    // Backward compatibility methods (deprecated - for migration period)
    @Deprecated
    public String getEmployeeID() {
        return employeeId;
    }

    @Deprecated
    public void setEmployeeID(String employeeID) {
        this.employeeId = employeeID;
    }

    @Deprecated
    public Date getAcctualDate() {
        return date;
    }

    @Deprecated
    public void setAcctualDate(Date acctualDate) {
        this.date = acctualDate;
    }

    @Deprecated
    public Date getHappenDate() {
        return date;
    }

    @Deprecated
    public void setHappenDate(Date happenDate) {
        this.date = happenDate;
    }

    @Override
    public String toString() {
        return "NoPayDTO{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", attendanceId=" + attendanceId +
                ", submissionDate=" + submissionDate +
                ", date=" + date +
                ", comment='" + comment + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}