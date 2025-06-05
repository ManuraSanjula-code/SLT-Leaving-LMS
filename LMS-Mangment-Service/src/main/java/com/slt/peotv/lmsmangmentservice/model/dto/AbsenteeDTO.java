package com.slt.peotv.lmsmangmentservice.model.dto;

import java.util.Date;

public class AbsenteeDTO {
    private Long id;
    private String publicId;
    private Date date;
    private String employeeID;
    private String userId;
    private Integer audited = 0;
    private Integer isNoPay = 0;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAudited() {
        return audited;
    }

    public void setAudited(Integer audited) {
        this.audited = audited;
    }

    public Integer getIsNoPay() {
        return isNoPay;
    }

    public void setIsNoPay(Integer isNoPay) {
        this.isNoPay = isNoPay;
    }
}
