package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.*;
import javax.persistence.*;
import java.sql.Time;
import java.util.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Entity
@Table(name = "attendance",uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "date","arrival_date","arrival_time"}))
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(nullable = false)
    private Date date;

    @Column(name = "arrival_date")
    private Date arrivalDate;

    @Column(name = "arrival_time")
    private Time arrivalTime;

    private Time leftTime;

    private String arrivalTimeRaw;
    private String leftTimeRaw;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId = "NONE";

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type")
    private AttendanceType attendanceType;

    @Enumerated(EnumType.STRING)
    private RosterType rosterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatus leaveStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status")
    private PayStatus payStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolve")
    private ResolveType resolve;

    private Boolean isLate = false;
    private Boolean isLateCovered = false;
    private Boolean isUnauthorized = false;
    private Boolean isUnSuccessful = false;
    private Boolean isHoliday = false;
    private Boolean isResolved = false;
    private Boolean hasIssues = false;
    private Boolean isManual = false;

    @Column(name = "issue_description", length = 1000)
    private String issueDescription;

    @Column(name = "due_date_for_ua")
    private Date dueDateForUA;

    @Column(name = "etl_run_time")
    private Date etlRunTime;

    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate = new Date();

    @Column(name = "is_active")
    private Boolean isActive = true;

    private Boolean viaMovement;
    private Boolean viaLeave;

    public AttendanceEntity() {
    }

    public AttendanceEntity(Long id, String publicId, EmployeeEntity employee, Date date, Date arrivalDate, Time arrivalTime, Time leftTime, String arrivalTimeRaw, String leftTimeRaw, String terminalId, AttendanceType attendanceType, RosterType rosterType, LeaveStatus leaveStatus, PayStatus payStatus, ResolveType resolve, Boolean isLate, Boolean isLateCovered, Boolean isUnauthorized, Boolean isUnSuccessful, Boolean isHoliday, Boolean isResolved, Boolean hasIssues, Boolean isManual, String issueDescription, Date dueDateForUA, Date etlRunTime, Date createdDate, Date updatedDate, Boolean isActive, Boolean viaMovement, Boolean viaLeave) {
        this.id = id;
        this.publicId = publicId;
        this.employee = employee;
        this.date = date;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.leftTime = leftTime;
        this.arrivalTimeRaw = arrivalTimeRaw;
        this.leftTimeRaw = leftTimeRaw;
        this.terminalId = terminalId != null ? terminalId : "NONE";
        this.attendanceType = attendanceType;
        this.rosterType = rosterType;
        this.leaveStatus = leaveStatus;
        this.payStatus = payStatus;
        this.resolve = resolve;
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
        this.etlRunTime = etlRunTime;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate != null ? updatedDate : new Date();
        this.isActive = isActive != null ? isActive : true;
        this.viaMovement = viaMovement;
        this.viaLeave = viaLeave;
    }

    public AttendanceEntity(String publicId, EmployeeEntity employee, Date date) {
        this.publicId = publicId;
        this.employee = employee;
        this.date = date;
        this.terminalId = "NONE";
        this.isLate = false;
        this.isLateCovered = false;
        this.isUnauthorized = false;
        this.isUnSuccessful = false;
        this.isHoliday = false;
        this.isResolved = false;
        this.hasIssues = false;
        this.isManual = false;
        this.createdDate = new Date();
        this.updatedDate = new Date();
        this.isActive = true;
    }

    public static AttendanceEntity create(String publicId, EmployeeEntity employee, Date date) {
        return new AttendanceEntity(publicId, employee, date);
    }

    public static AttendanceEntity createManual(String publicId, EmployeeEntity employee, Date date, Date etlRunTime) {
        AttendanceEntity entity = new AttendanceEntity(publicId, employee, date);
        entity.setManual(true);
        entity.setEtlRunTime(etlRunTime);
        return entity;
    }

    public static AttendanceEntity createWithDefaults(String publicId, EmployeeEntity employee, Date date) {
        AttendanceEntity entity = new AttendanceEntity();
        entity.setPublicId(publicId);
        entity.setEmployee(employee);
        entity.setDate(date);
        entity.setTerminalId("NONE");
        entity.setLate(false);
        entity.setLateCovered(false);
        entity.setUnauthorized(false);
        entity.setUnSuccessful(false);
        entity.setHoliday(false);
        entity.setResolved(false);
        entity.setHasIssues(false);
        entity.setManual(false);
        entity.setCreatedDate(new Date());
        entity.setUpdatedDate(new Date());
        entity.setActive(true);
        return entity;
    }

    public Boolean getIsFullDay() {
        return attendanceType != null && attendanceType.equals(AttendanceType.FULL_DAY);
    }

    public Boolean isArrivalOnWeekend() {
        if (this.arrivalDate == null) {
            return false;
        }

        LocalDate localArrivalDate = this.arrivalDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        DayOfWeek dayOfWeek = localArrivalDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
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

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceEntity that = (AttendanceEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(publicId, that.publicId) && Objects.equals(employee, that.employee) && Objects.equals(date, that.date) && Objects.equals(arrivalDate, that.arrivalDate) && Objects.equals(arrivalTime, that.arrivalTime) && Objects.equals(leftTime, that.leftTime) && Objects.equals(arrivalTimeRaw, that.arrivalTimeRaw) && Objects.equals(leftTimeRaw, that.leftTimeRaw) && Objects.equals(terminalId, that.terminalId) && attendanceType == that.attendanceType && rosterType == that.rosterType && leaveStatus == that.leaveStatus && payStatus == that.payStatus && resolve == that.resolve && Objects.equals(isLate, that.isLate) && Objects.equals(isLateCovered, that.isLateCovered) && Objects.equals(isUnauthorized, that.isUnauthorized) && Objects.equals(isUnSuccessful, that.isUnSuccessful) && Objects.equals(isHoliday, that.isHoliday) && Objects.equals(isResolved, that.isResolved) && Objects.equals(hasIssues, that.hasIssues) && Objects.equals(isManual, that.isManual) && Objects.equals(issueDescription, that.issueDescription) && Objects.equals(dueDateForUA, that.dueDateForUA) && Objects.equals(etlRunTime, that.etlRunTime) && Objects.equals(createdDate, that.createdDate) && Objects.equals(updatedDate, that.updatedDate) && Objects.equals(isActive, that.isActive) && Objects.equals(viaMovement, that.viaMovement) && Objects.equals(viaLeave, that.viaLeave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, employee, date, arrivalDate, arrivalTime, leftTime, arrivalTimeRaw, leftTimeRaw, terminalId, attendanceType, rosterType, leaveStatus, payStatus, resolve, isLate, isLateCovered, isUnauthorized, isUnSuccessful, isHoliday, isResolved, hasIssues, isManual, issueDescription, dueDateForUA, etlRunTime, createdDate, updatedDate, isActive, viaMovement, viaLeave);
    }
}