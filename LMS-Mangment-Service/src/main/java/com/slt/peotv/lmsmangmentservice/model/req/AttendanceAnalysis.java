package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;

public class AttendanceAnalysis {
    final AttendanceType attendanceType;
    final boolean isAuthorized;
    final boolean isLate;
    final boolean isUnsuccessful;
    final String issueDescription;

    public AttendanceAnalysis(AttendanceType attendanceType, boolean isAuthorized,
                              boolean isLate, boolean isUnsuccessful, String issueDescription) {
        this.attendanceType = attendanceType;
        this.isAuthorized = isAuthorized;
        this.isLate = isLate;
        this.isUnsuccessful = isUnsuccessful;
        this.issueDescription = issueDescription;
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
}
