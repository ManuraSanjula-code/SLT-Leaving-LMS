package com.slt.peotv.lmsmangmentservice.entity.Leave;

import java.util.Date;

public class LeaveTra {

    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String leaveId;
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

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
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
