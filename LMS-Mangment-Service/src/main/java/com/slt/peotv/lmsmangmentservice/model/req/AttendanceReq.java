package com.slt.peotv.lmsmangmentservice.model.req;

import lombok.Data;

import java.sql.Time;
import java.util.Date;

@Data
public class AttendanceReq {
    private Date date;
    private String employeeID;
    private Boolean isFullDay = false;
    private Date arrivalDate;
    private Time arrivalTime;
    private Time leftTime;
    private Boolean isLate = false;
    private Boolean lateCover = false;
    private Boolean isHalfDay = false;
    private Boolean isFullLeave = false;
    private Boolean isShortLeave = false;
    private Boolean isAbsent = false;
    private Boolean isUnSuccessful = false;
    private Boolean isNoPay = false;
    private Boolean issues = false;
    private Boolean isUnAuthorized = false;
    private Boolean resolve = false;
    private Boolean leaveSuccess = false;
    private Boolean leaveReq = false;
    private String issueDescription;
    private Date dueDateForUA;
    private Boolean active = true;
    private Boolean nopay = false;
    private String terminalID;
    private String adminId;
    private String adminComment;
    private Boolean viaMovement;
    private Boolean viaLeave;
}