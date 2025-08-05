package com.slt.peotv.lmsmangmentservice.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.PayStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ResolveType;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Date;
import java.util.Objects;

@Data
@ToString
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

    public boolean validateAttendanceReq() {
        if (Objects.isNull(this.employeeID) || this.employeeID.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.date)) {
            return false;
        }

        if (Objects.isNull(this.terminalID) || this.terminalID.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.attendanceType)) {
            return false;
        }

        if (!validateDateLogic()) {
            return false;
        }

        if (!validateTimeLogic()) {
            return false;
        }

        if (!validateAttendanceTypeConsistency()) {
            return false;
        }

        if (!validateStatusConsistency()) {
            return false;
        }

        return true;
    }


    private boolean validateDateLogic() {
        Date today = new Date();
        if (this.date != null && this.date.after(today)) {
            return false;
        }

        if (this.arrivalDate != null && this.date != null) {
            long diffInDays = Math.abs(this.arrivalDate.getTime() - this.date.getTime()) / (24 * 60 * 60 * 1000);
            return diffInDays <= 1;
        }

        return true;
    }

    private boolean validateTimeLogic() {
        // If both arrival and left times are provided, left time should be after arrival time
        if (this.arrivalTime != null && this.leftTime != null) {
            return this.leftTime.after(this.arrivalTime);
        }

        if (this.attendanceType == AttendanceType.FULL_DAY) {
            return this.arrivalTime != null && this.leftTime != null;
        }

        if (this.attendanceType == AttendanceType.HALF_DAY) {
            return this.arrivalTime != null;
        }

        return true;
    }


    private boolean validateAttendanceTypeConsistency() {
        if (this.attendanceType == null) {
            return false;
        }

        switch (this.attendanceType) {
            case ABSENT:
                return this.arrivalTime == null && this.leftTime == null;

            case FULL_DAY:
                return true;

            case HALF_DAY:
                return this.arrivalTime != null;

            default:
                return true;
        }
    }


    private boolean validateStatusConsistency() {
        if (Boolean.TRUE.equals(this.hasIssues)) {
            return this.issueDescription != null && !this.issueDescription.trim().isEmpty();
        }

        if (Boolean.TRUE.equals(this.isUnauthorized)) {
            return this.dueDateForUA != null;
        }

        if (Boolean.TRUE.equals(this.isResolved)) {
            return this.resolve != null;
        }

        if (this.payStatus == PayStatus.NO_PAY) {
            return this.attendanceType == AttendanceType.ABSENT ||
                    Boolean.TRUE.equals(this.isUnauthorized);
        }

        return true;
    }


    public boolean hasRequiredFields() {
        return Objects.nonNull(this.employeeID) && !this.employeeID.trim().isEmpty() &&
                Objects.nonNull(this.date) &&
                Objects.nonNull(this.terminalID) && !this.terminalID.trim().isEmpty() &&
                Objects.nonNull(this.attendanceType);
    }

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
        // Working hours should be reasonable (not more than 24 hours)
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
}