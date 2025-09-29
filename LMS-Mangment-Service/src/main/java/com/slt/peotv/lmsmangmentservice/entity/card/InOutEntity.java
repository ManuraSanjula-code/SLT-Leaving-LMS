package com.slt.peotv.lmsmangmentservice.entity.card;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.InOutType;
import javax.persistence.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "in_out",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"employee_id", "punch_time", "punch_type_time", "terminal_id"}))
public class InOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private Date date;

    @Column(name = "punch_time", nullable = false)
    private Date punchTime;

    @Column(name = "punch_type_time")
    private Time punchTypeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "in_out_type", nullable = false)
    private InOutType inOutType;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "in_out_value")
    private Integer inOutValue = -1;

    @Column(name = "is_manual")
    private Boolean isManual = false;

    @Column(name = "etl_run_time")
    private Date etlRunTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "access_log_id")
    private AccessLogEntity accessLog;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public InOutEntity() {
    }

    public InOutEntity(Long id, String employeeId, Date date, Date punchTime, Time punchTypeTime, InOutType inOutType, String terminalId, Integer inOutValue, Boolean isManual, Date etlRunTime, AttendanceEntity attendance, AccessLogEntity accessLog, Date createdDate, Date updatedDate, Boolean isActive) {
        this.id = id;
        this.employeeId = employeeId;
        this.date = date;
        this.punchTime = punchTime;
        this.punchTypeTime = punchTypeTime;
        this.inOutType = inOutType;
        this.terminalId = terminalId;
        this.inOutValue = inOutValue != null ? inOutValue : -1;
        this.isManual = isManual != null ? isManual : false;
        this.etlRunTime = etlRunTime;
        this.attendance = attendance;
        this.accessLog = accessLog;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate;
        this.isActive = isActive != null ? isActive : true;
    }

    public InOutEntity(String employeeId, Date date, Date punchTime, InOutType inOutType, String terminalId) {
        this.employeeId = employeeId;
        this.date = date;
        this.punchTime = punchTime;
        this.inOutType = inOutType;
        this.terminalId = terminalId;
        this.inOutValue = -1;
        this.isManual = false;
        this.createdDate = new Date();
        this.isActive = true;
    }

    public InOutEntity(String employeeId, Date date, Date punchTime, Time punchTypeTime, InOutType inOutType, String terminalId) {
        this(employeeId, date, punchTime, inOutType, terminalId);
        this.punchTypeTime = punchTypeTime;
    }

    public static InOutEntity create(String employeeId, Date date, Date punchTime, InOutType inOutType, String terminalId) {
        return new InOutEntity(employeeId, date, punchTime, inOutType, terminalId);
    }

    public static InOutEntity createWithPunchTypeTime(String employeeId, Date date, Date punchTime, Time punchTypeTime, InOutType inOutType, String terminalId) {
        return new InOutEntity(employeeId, date, punchTime, punchTypeTime, inOutType, terminalId);
    }

    public static InOutEntity createManual(String employeeId, Date date, Date punchTime, InOutType inOutType, String terminalId) {
        InOutEntity entity = new InOutEntity(employeeId, date, punchTime, inOutType, terminalId);
        entity.setManual(true);
        return entity;
    }

    public static InOutEntity createWithDefaults(String employeeId, Date date, Date punchTime, InOutType inOutType, String terminalId) {
        InOutEntity entity = new InOutEntity();
        entity.setEmployeeId(employeeId);
        entity.setDate(date);
        entity.setPunchTime(punchTime);
        entity.setInOutType(inOutType);
        entity.setTerminalId(terminalId);
        entity.setInOutValue(-1);
        entity.setManual(false);
        entity.setCreatedDate(new Date());
        entity.setActive(true);
        return entity;
    }

    public String getPunchTypeTimeAsString() {
        if (punchTypeTime == null) {
            return null;
        }
        return new SimpleDateFormat("HH:mm:ss").format(punchTypeTime);
    }

    public boolean isPunchTimeAfter1730() {
        if (punchTypeTime == null) {
            return false;
        }

        LocalTime punchLocalTime = punchTypeTime.toLocalTime();
        LocalTime targetTime = LocalTime.of(17, 29, 59);

        return targetTime.isAfter(punchLocalTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(Date punchTime) {
        this.punchTime = punchTime;
    }

    public Time getPunchTypeTime() {
        return punchTypeTime;
    }

    public void setPunchTypeTime(Time punchTypeTime) {
        this.punchTypeTime = punchTypeTime;
    }

    public InOutType getInOutType() {
        return inOutType;
    }

    public void setInOutType(InOutType inOutType) {
        this.inOutType = inOutType;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public Integer getInOutValue() {
        return inOutValue;
    }

    public void setInOutValue(Integer inOutValue) {
        this.inOutValue = inOutValue;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

    public Date getEtlRunTime() {
        return etlRunTime;
    }

    public void setEtlRunTime(Date etlRunTime) {
        this.etlRunTime = etlRunTime;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }

    public AccessLogEntity getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(AccessLogEntity accessLog) {
        this.accessLog = accessLog;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InOutEntity that = (InOutEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(employeeId, that.employeeId) && Objects.equals(date, that.date) && Objects.equals(punchTime, that.punchTime) && Objects.equals(punchTypeTime, that.punchTypeTime) && inOutType == that.inOutType && Objects.equals(terminalId, that.terminalId) && Objects.equals(inOutValue, that.inOutValue) && Objects.equals(isManual, that.isManual) && Objects.equals(etlRunTime, that.etlRunTime) && Objects.equals(attendance, that.attendance) && Objects.equals(accessLog, that.accessLog) && Objects.equals(createdDate, that.createdDate) && Objects.equals(updatedDate, that.updatedDate) && Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, date, punchTime, punchTypeTime, inOutType, terminalId, inOutValue, isManual, etlRunTime, attendance, accessLog, createdDate, updatedDate, isActive);
    }
}