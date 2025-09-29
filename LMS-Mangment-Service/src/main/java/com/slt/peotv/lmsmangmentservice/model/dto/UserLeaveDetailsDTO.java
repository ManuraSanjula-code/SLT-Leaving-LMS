package com.slt.peotv.lmsmangmentservice.model.dto;


import java.util.List;

public class UserLeaveDetailsDTO {
    private String employeeId;
    private List<LeaveDetailDTO> leaveDetails;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<LeaveDetailDTO> getLeaveDetails() {
        return leaveDetails;
    }

    public void setLeaveDetails(List<LeaveDetailDTO> leaveDetails) {
        this.leaveDetails = leaveDetails;
    }
}