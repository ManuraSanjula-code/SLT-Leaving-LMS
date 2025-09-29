package com.slt.radio.rosterservice.documents.one.lms;

import java.io.Serializable;
import com.slt.radio.rosterservice.documents.enums.AttendanceType;
import com.slt.radio.rosterservice.documents.enums.RosterType;
import com.slt.radio.rosterservice.documents.enums.LeaveStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;

@Document(collection = "attendances")
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1328292L;

    @Id
    private String id;
    private String publicId;
    private Date date;
    private Date arrivalDate;

    private LocalTime arrivalTime;
    private LocalTime leftTime;

    private String terminalId;
    private String employeeId;

    private String teamId;
    private AttendanceType attendanceType;
    private RosterType rosterType;
    private LeaveStatus leaveStatus;

    private Boolean isLate = false;
    private Boolean isLateCovered = false;
    private Boolean isUnauthorized = false;
    private Boolean isUnSuccessful = false;
    private Boolean isHoliday = false;
    private Boolean isResolved = false;
    private Boolean hasIssues = false;
    private Boolean isManual = false;

    private String issueDescription;
    private Date dueDateForUA;

    private Date etlRunTime = new Date();
    private Date createdDate = new Date();
    private Date updatedDate = new Date();
    private Boolean isActive = true;
    private Boolean viaMovement;
    private Boolean viaLeave;

    public Attendance() {}

    public Attendance(String id, String publicId, Date date, Date arrivalDate,
                      LocalTime arrivalTime, LocalTime leftTime, String terminalId,
                      String employeeId, String teamId, AttendanceType attendanceType,
                      RosterType rosterType, LeaveStatus leaveStatus, Boolean isLate,
                      Boolean isLateCovered, Boolean isUnauthorized, Boolean isUnSuccessful,
                      Boolean isHoliday, Boolean isResolved, Boolean hasIssues,
                      Boolean isManual, String issueDescription, Date dueDateForUA,
                      Date etlRunTime, Date createdDate, Date updatedDate,
                      Boolean isActive, Boolean viaMovement, Boolean viaLeave) {
        this.id = id;
        this.publicId = publicId;
        this.date = date;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.leftTime = leftTime;
        this.terminalId = terminalId;
        this.employeeId = employeeId;
        this.teamId = teamId;
        this.attendanceType = attendanceType;
        this.rosterType = rosterType;
        this.leaveStatus = leaveStatus;
        this.isLate = isLate != null ? isLate : false;
        this.isLateCovered = isLateCovered != null ? isLateCovered : false;
        this.isUnauthorized = isUnauthorized != null ? isUnauthorized : false;
        this.isUnSuccessful = isUnSuccessful != null ? isUnSuccessful : false;
        this.isHoliday = isHoliday != null ? isHoliday : false;
        this.isResolved = isResolved != null ? isResolved : false;
        this.hasIssues = hasIssues != null ? hasIssues : false;
        this.isManual = isManual != null ? isManual : false;
        this.issueDescription = issueDescription;
        this.dueDateForUA = dueDateForUA;
        this.etlRunTime = etlRunTime != null ? etlRunTime : new Date();
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate != null ? updatedDate : new Date();
        this.isActive = isActive != null ? isActive : true;
        this.viaMovement = viaMovement;
        this.viaLeave = viaLeave;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
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

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(LocalTime leftTime) {
        this.leftTime = leftTime;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public AttendanceType getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(AttendanceType attendanceType) {
        this.attendanceType = attendanceType;
    }

    public RosterType getRosterType() {
        return rosterType;
    }

    public void setRosterType(RosterType rosterType) {
        this.rosterType = rosterType;
    }

    public LeaveStatus getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(LeaveStatus leaveStatus) {
        this.leaveStatus = leaveStatus;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    // Builder pattern
    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }

    public static class AttendanceBuilder {
        private String id;
        private String publicId;
        private Date date;
        private Date arrivalDate;
        private LocalTime arrivalTime;
        private LocalTime leftTime;
        private String terminalId;
        private String employeeId;
        private String teamId;
        private AttendanceType attendanceType;
        private RosterType rosterType;
        private LeaveStatus leaveStatus;
        private Boolean isLate = false;
        private Boolean isLateCovered = false;
        private Boolean isUnauthorized = false;
        private Boolean isUnSuccessful = false;
        private Boolean isHoliday = false;
        private Boolean isResolved = false;
        private Boolean hasIssues = false;
        private Boolean isManual = false;
        private String issueDescription;
        private Date dueDateForUA;
        private Date etlRunTime = new Date();
        private Date createdDate = new Date();
        private Date updatedDate = new Date();
        private Boolean isActive = true;
        private Boolean viaMovement;
        private Boolean viaLeave;

        public AttendanceBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AttendanceBuilder publicId(String publicId) {
            this.publicId = publicId;
            return this;
        }

        public AttendanceBuilder date(Date date) {
            this.date = date;
            return this;
        }

        public AttendanceBuilder arrivalDate(Date arrivalDate) {
            this.arrivalDate = arrivalDate;
            return this;
        }

        public AttendanceBuilder arrivalTime(LocalTime arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public AttendanceBuilder leftTime(LocalTime leftTime) {
            this.leftTime = leftTime;
            return this;
        }

        public AttendanceBuilder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public AttendanceBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public AttendanceBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public AttendanceBuilder attendanceType(AttendanceType attendanceType) {
            this.attendanceType = attendanceType;
            return this;
        }

        public AttendanceBuilder rosterType(RosterType rosterType) {
            this.rosterType = rosterType;
            return this;
        }

        public AttendanceBuilder leaveStatus(LeaveStatus leaveStatus) {
            this.leaveStatus = leaveStatus;
            return this;
        }

        public AttendanceBuilder isLate(Boolean isLate) {
            this.isLate = isLate;
            return this;
        }

        public AttendanceBuilder isLateCovered(Boolean isLateCovered) {
            this.isLateCovered = isLateCovered;
            return this;
        }

        public AttendanceBuilder isUnauthorized(Boolean isUnauthorized) {
            this.isUnauthorized = isUnauthorized;
            return this;
        }

        public AttendanceBuilder isUnSuccessful(Boolean isUnSuccessful) {
            this.isUnSuccessful = isUnSuccessful;
            return this;
        }

        public AttendanceBuilder isHoliday(Boolean isHoliday) {
            this.isHoliday = isHoliday;
            return this;
        }

        public AttendanceBuilder isResolved(Boolean isResolved) {
            this.isResolved = isResolved;
            return this;
        }

        public AttendanceBuilder hasIssues(Boolean hasIssues) {
            this.hasIssues = hasIssues;
            return this;
        }

        public AttendanceBuilder isManual(Boolean isManual) {
            this.isManual = isManual;
            return this;
        }

        public AttendanceBuilder issueDescription(String issueDescription) {
            this.issueDescription = issueDescription;
            return this;
        }

        public AttendanceBuilder dueDateForUA(Date dueDateForUA) {
            this.dueDateForUA = dueDateForUA;
            return this;
        }

        public AttendanceBuilder etlRunTime(Date etlRunTime) {
            this.etlRunTime = etlRunTime;
            return this;
        }

        public AttendanceBuilder createdDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public AttendanceBuilder updatedDate(Date updatedDate) {
            this.updatedDate = updatedDate;
            return this;
        }

        public AttendanceBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AttendanceBuilder viaMovement(Boolean viaMovement) {
            this.viaMovement = viaMovement;
            return this;
        }

        public AttendanceBuilder viaLeave(Boolean viaLeave) {
            this.viaLeave = viaLeave;
            return this;
        }

        public Attendance build() {
            return new Attendance(id, publicId, date, arrivalDate, arrivalTime, leftTime,
                    terminalId, employeeId, teamId, attendanceType, rosterType,
                    leaveStatus, isLate, isLateCovered, isUnauthorized,
                    isUnSuccessful, isHoliday, isResolved, hasIssues, isManual,
                    issueDescription, dueDateForUA, etlRunTime, createdDate,
                    updatedDate, isActive, viaMovement, viaLeave);
        }
    }
}