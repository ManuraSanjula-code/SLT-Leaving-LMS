package com.slt.peotv.lmsmangmentservice.model.dto;

import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.PayStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ResolveType;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public class AttendanceDTO {
    private Long id;
    private String publicId;
    private String employeeId;
    private String userId;
    private Date date;
    private Date arrivalDate;
    private Time arrivalTime;
    private Time leftTime;
    private String terminalId;

    private AttendanceType attendanceType;
    private LeaveStatus leaveStatus;
    private PayStatus payStatus;
    private ResolveType resolve;

    private Boolean isLate = false;
    private Boolean isLateCovered = false;
    private Boolean isUnauthorized = false;
    private Boolean isUnSuccessful = false;
    private Boolean isHoliday = false;
    private Boolean isResolved = false;
    private Boolean hasIssues = false;
    private Boolean isManual = false;
    private Boolean isActive = true;

    private String issueDescription;
    private Date dueDateForUA;
    private Date etlRunTime;
    private Date createdDate;
    private Date updatedDate;

    private List<InOutDTO> inOutDTOs;

    private Boolean viaMovement;
    private Boolean viaLeave;

    public AttendanceDTO() {}

    public AttendanceDTO(Long id, String publicId, String employeeId, Date date) {
        this.id = id;
        this.publicId = publicId;
        this.employeeId = employeeId;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
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

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Boolean getIsLateCovered() {
        return isLateCovered;
    }

    public void setIsLateCovered(Boolean isLateCovered) {
        this.isLateCovered = isLateCovered;
    }

    public Boolean getIsUnauthorized() {
        return isUnauthorized;
    }

    public void setIsUnauthorized(Boolean isUnauthorized) {
        this.isUnauthorized = isUnauthorized;
    }

    public Boolean getIsUnSuccessful() {
        return isUnSuccessful;
    }

    public void setIsUnSuccessful(Boolean isUnSuccessful) {
        this.isUnSuccessful = isUnSuccessful;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public Boolean getIsManual() {
        return isManual;
    }

    public void setIsManual(Boolean isManual) {
        this.isManual = isManual;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<InOutDTO> getInOutDTOs() {
        return inOutDTOs;
    }

    public void setInOutDTOs(List<InOutDTO> inOutDTOs) {
        this.inOutDTOs = inOutDTOs;
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

    // Convenience methods
    public Boolean getIsFullDay() {
        return attendanceType != null && attendanceType.equals(AttendanceType.FULL_DAY);
    }

    public Boolean getIsHalfDay() {
        return attendanceType != null && attendanceType.equals(AttendanceType.HALF_DAY);
    }

    public Boolean getIsAbsent() {
        return attendanceType != null && attendanceType.equals(AttendanceType.ABSENT);
    }

    public Boolean getIsNoPay() {
        return payStatus != null && payStatus.equals(PayStatus.NO_PAY);
    }

    @Override
    public String toString() {
        return "AttendanceDTO{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", date=" + date +
                ", attendanceType=" + attendanceType +
                ", leaveStatus=" + leaveStatus +
                ", payStatus=" + payStatus +
                ", arrivalTime=" + arrivalTime +
                ", leftTime=" + leftTime +
                ", isLate=" + isLate +
                ", isActive=" + isActive +
                '}';
    }
}