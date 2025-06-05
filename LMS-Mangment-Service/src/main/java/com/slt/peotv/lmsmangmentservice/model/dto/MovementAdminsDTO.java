package com.slt.peotv.lmsmangmentservice.model.dto;


import java.util.Date;

public class MovementAdminsDTO {

    private Integer id;
    private String movementId;
    private String userId;
    private String sltId;
    private String employeeId;
    private Date approvedDate;
    private Integer highestRolePriority;
    private Boolean isAccepted;

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Integer getHighestRolePriority() {
        return highestRolePriority;
    }

    public void setHighestRolePriority(Integer highestRolePriority) {
        this.highestRolePriority = highestRolePriority;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMovementId() {
        return movementId;
    }

    public void setMovementId(String movementId) {
        this.movementId = movementId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSltId() {
        return sltId;
    }

    public void setSltId(String sltId) {
        this.sltId = sltId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }
}
