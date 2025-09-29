package com.slt.peotv.lmsmangmentservice.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.PayStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ResolveType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Date;

public class AttendanceReq {

    private String publicId;
    private Long id;

    @NotBlank(message = "Employee ID is required")
    @JsonProperty("employeeId")
    private String employeeID;

    @NotNull(message = "Date is required")
    @JsonProperty("date")
    private Date date;

    @JsonProperty("arrivalDate")
    private Date arrivalDate;

    @JsonProperty("arrivalTime")
    private Time arrivalTime;

    @JsonProperty("leftTime")
    private Time leftTime;

    @NotBlank(message = "Terminal ID is required")
    @JsonProperty("terminalId")
    private String terminalID;

    @NotNull(message = "Attendance type is required")
    private AttendanceType attendanceType;

    @JsonProperty("leaveStatus")
    private LeaveStatus leaveStatus;

    @JsonProperty("payStatus")
    private PayStatus payStatus;

    @JsonProperty("resolve")
    private ResolveType resolve;

    @JsonProperty("isLate")
    private Boolean isLate = false;

    @JsonProperty("isLateCovered")
    private Boolean isLateCovered = false;

    @JsonProperty("isUnauthorized")
    private Boolean isUnauthorized = false;

    @JsonProperty("isUnSuccessful")
    private Boolean isUnSuccessful = false;

    @JsonProperty("isHoliday")
    private Boolean isHoliday = false;

    @JsonProperty("isResolved")
    private Boolean isResolved = false;

    @JsonProperty("hasIssues")
    private Boolean hasIssues = false;

    @JsonProperty("isManual")
    private Boolean isManual = false;

    @JsonProperty("issueDescription")
    private String issueDescription;

    @JsonProperty("dueDateForUA")
    private Date dueDateForUA;

    @JsonProperty("etlRunTime")
    private Date etlRunTime;

    private Date createdDate;
    private Date updatedDate;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    @JsonProperty("viaMovement")
    private Boolean viaMovement = false;

    @JsonProperty("viaLeave")
    private Boolean viaLeave = false;

    @JsonProperty("arrivalTimeRaw")
    private String arrivalTimeRaw;

    @JsonProperty("leftTimeRaw")
    private String leftTimeRaw;

    public boolean isFullDayAttendance() {
        return this.attendanceType == AttendanceType.FULL_DAY;
    }


    public boolean isHalfDayAttendance() {
        return this.attendanceType == AttendanceType.HALF_DAY;
    }

    public boolean isAbsentRecord() {
        return this.attendanceType == AttendanceType.ABSENT;
    }

    public boolean hasAttendanceIssues() {
        return Boolean.TRUE.equals(this.hasIssues) ||
                Boolean.TRUE.equals(this.isUnauthorized) ||
                Boolean.TRUE.equals(this.isUnSuccessful);
    }

    public String getAttendanceStatusString() {
        if (this.attendanceType != null) {
            return this.attendanceType.getDescription();
        }
        return "Unknown";
    }

    public String getLeaveStatusString() {
        if (this.leaveStatus != null) {
            return this.leaveStatus.getDescription();
        }
        return "No Leave";
    }

    public double getWorkingHours() {
        if (this.arrivalTime != null && this.leftTime != null) {
            long diffInMillis = this.leftTime.getTime() - this.arrivalTime.getTime();
            return diffInMillis / (1000.0 * 60 * 60); // Convert to hours
        }
        return 0.0;
    }

    public boolean canBeEdited() {
        return Boolean.TRUE.equals(this.isActive) &&
                !Boolean.TRUE.equals(this.isResolved);
    }

    public boolean validateBusinessRules() {
        double workingHours = getWorkingHours();
        if (workingHours > 24) {
            return false;
        }

        if (this.issueDescription != null && this.issueDescription.length() > 1000) {
            return false;
        }

        if (this.dueDateForUA != null) {
            Date today = new Date();
            return !this.dueDateForUA.before(today);
        }

        return true;
    }



    public Boolean getIsFullDay() {
        return attendanceType == AttendanceType.FULL_DAY;
    }


    public void setIsFullDay(Boolean isFullDay) {
        if (Boolean.TRUE.equals(isFullDay)) {
            this.attendanceType = AttendanceType.FULL_DAY;
        }
    }


    public Boolean getIsHalfDay() {
        return attendanceType == AttendanceType.HALF_DAY;
    }


    public void setIsHalfDay(Boolean isHalfDay) {
        if (Boolean.TRUE.equals(isHalfDay)) {
            this.attendanceType = AttendanceType.HALF_DAY;
        }
    }


    public Boolean getIsAbsent() {
        return attendanceType == AttendanceType.ABSENT;
    }


    public void setIsAbsent(Boolean isAbsent) {
        if (Boolean.TRUE.equals(isAbsent)) {
            this.attendanceType = AttendanceType.ABSENT;
        }
    }


    public Boolean getIsNoPay() {
        return payStatus == PayStatus.NO_PAY;
    }

    public void setIsNoPay(Boolean isNoPay) {
        if (Boolean.TRUE.equals(isNoPay)) {
            this.payStatus = PayStatus.NO_PAY;
        } else {
            this.payStatus = null;
        }
    }

    public Boolean getLateCover() {
        return this.isLateCovered;
    }
    public void setLateCover(Boolean lateCover) {
        this.isLateCovered = lateCover;
    }
    public Boolean getIssues() {
        return this.hasIssues;
    }

    public void setIssues(Boolean issues) {
        this.hasIssues = issues;
    }
    public Boolean getActive() {
        return this.isActive;
    }
    public void setActive(Boolean active) {
        this.isActive = active;
    }
    public Boolean getIsUnAuthorized() {
        return this.isUnauthorized;
    }
    public void setIsUnAuthorized(Boolean isUnAuthorized) {
        this.isUnauthorized = isUnAuthorized;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Time getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(Time leftTime) {
        this.leftTime = leftTime;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public AttendanceType getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(AttendanceType attendanceType) {
        this.attendanceType = attendanceType;
    }

    public LeaveStatus getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(LeaveStatus leaveStatus) {
        this.leaveStatus = leaveStatus;
    }

    public PayStatus getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(PayStatus payStatus) {
        this.payStatus = payStatus;
    }

    public ResolveType getResolve() {
        return resolve;
    }

    public void setResolve(ResolveType resolve) {
        this.resolve = resolve;
    }

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getLateCovered() {
        return isLateCovered;
    }

    public void setLateCovered(Boolean lateCovered) {
        isLateCovered = lateCovered;
    }

    public Boolean getUnauthorized() {
        return isUnauthorized;
    }

    public void setUnauthorized(Boolean unauthorized) {
        isUnauthorized = unauthorized;
    }

    public Boolean getUnSuccessful() {
        return isUnSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        isUnSuccessful = unSuccessful;
    }

    public Boolean getHoliday() {
        return isHoliday;
    }

    public void setHoliday(Boolean holiday) {
        isHoliday = holiday;
    }

    public Boolean getResolved() {
        return isResolved;
    }

    public void setResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public Date getDueDateForUA() {
        return dueDateForUA;
    }

    public void setDueDateForUA(Date dueDateForUA) {
        this.dueDateForUA = dueDateForUA;
    }

    public Date getEtlRunTime() {
        return etlRunTime;
    }

    public void setEtlRunTime(Date etlRunTime) {
        this.etlRunTime = etlRunTime;
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

    public Boolean getViaMovement() {
        return viaMovement;
    }

    public void setViaMovement(Boolean viaMovement) {
        this.viaMovement = viaMovement;
    }

    public Boolean getViaLeave() {
        return viaLeave;
    }

    public void setViaLeave(Boolean viaLeave) {
        this.viaLeave = viaLeave;
    }

    public String getArrivalTimeRaw() {
        return arrivalTimeRaw;
    }

    public void setArrivalTimeRaw(String arrivalTimeRaw) {
        this.arrivalTimeRaw = arrivalTimeRaw;
    }

    public String getLeftTimeRaw() {
        return leftTimeRaw;
    }

    public void setLeftTimeRaw(String leftTimeRaw) {
        this.leftTimeRaw = leftTimeRaw;
    }
}