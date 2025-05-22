package com.slt.peotv.lmsmangmentservice.entity.Movement;

import java.util.Date;

public class MovementTra {

    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String movementId;
    private String userId;
    private String sltId;
    private String employeeId;
    private Date approvedDate;
    private Integer highestRolePriority;
    private Boolean isAccepted;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Integer getHighestRolePriority() {
        return highestRolePriority;
    }

    public void setHighestRolePriority(Integer highestRolePriority) {
        this.highestRolePriority = highestRolePriority;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }
}
