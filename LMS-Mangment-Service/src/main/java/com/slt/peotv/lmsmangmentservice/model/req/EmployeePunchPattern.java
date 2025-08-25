package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;

import java.util.ArrayList;
import java.util.List;

public class EmployeePunchPattern {
    private final String employeeId;
    private final List<InOutEntity> allPunches;
    private final InOutEntity firstInPunch;
    private final InOutEntity lastOutPunch;
    private final AttendanceType attendanceType;
    private final boolean isAuthorized;
    private final boolean isLate;
    private final boolean isUnsuccessful;
    private final String issueDescription;

    public EmployeePunchPattern(String employeeId, List<InOutEntity> allPunches,
                                InOutEntity firstInPunch, InOutEntity lastOutPunch,
                                AttendanceType attendanceType, boolean isAuthorized,
                                boolean isLate, boolean isUnsuccessful, String issueDescription) {
        this.employeeId = employeeId;
        this.allPunches = new ArrayList<>(allPunches);
        this.firstInPunch = firstInPunch;
        this.lastOutPunch = lastOutPunch;
        this.attendanceType = attendanceType;
        this.isAuthorized = isAuthorized;
        this.isLate = isLate;
        this.isUnsuccessful = isUnsuccessful;
        this.issueDescription = issueDescription;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public List<InOutEntity> getAllPunches() {
        return allPunches;
    }

    public InOutEntity getFirstInPunch() {
        return firstInPunch;
    }

    public InOutEntity getLastOutPunch() {
        return lastOutPunch;
    }

    public AttendanceType getAttendanceType() {
        return attendanceType;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public boolean isLate() {
        return isLate;
    }

    public boolean isUnsuccessful() {
        return isUnsuccessful;
    }

    public String getIssueDescription() {
        return issueDescription;
    }


    public boolean hasValidInOutPair() {
        return firstInPunch != null && lastOutPunch != null &&
                !firstInPunch.getPunchTypeTime().equals(lastOutPunch.getPunchTypeTime());
    }
}