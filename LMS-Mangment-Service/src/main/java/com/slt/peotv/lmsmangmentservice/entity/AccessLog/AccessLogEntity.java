package com.slt.peotv.lmsmangmentservice.entity.AccessLog;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "access_log",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"employee_id", "log_date", "log_time","terminal_id"}))
public class AccessLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "log_date", nullable = false)
    private String logDate;

    @Column(name = "log_time", nullable = false)
    private String logTime;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "in_out", nullable = false)
    private String inOut;

    @Column(name = "read_status", nullable = false)
    private String readStatus;

    @Column(name = "processed", nullable = false)
    private Integer processed;

    @Column(name = "etl_run_time", nullable = false)
    private Date etlRunTime;

    @Column(name = "is_manual")
    private Boolean isManual = false;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public AccessLogEntity() {
    }

    public AccessLogEntity(Long id, String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime, Boolean isManual, Date createdDate, Date updatedDate, Boolean isActive) {
        this.id = id;
        this.employeeId = employeeId;
        this.logDate = logDate;
        this.logTime = logTime;
        this.terminalId = terminalId;
        this.inOut = inOut;
        this.readStatus = readStatus;
        this.processed = processed;
        this.etlRunTime = etlRunTime;
        this.isManual = isManual != null ? isManual : false;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate;
        this.isActive = isActive != null ? isActive : true;
    }

    public AccessLogEntity(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        this.employeeId = employeeId;
        this.logDate = logDate;
        this.logTime = logTime;
        this.terminalId = terminalId;
        this.inOut = inOut;
        this.readStatus = readStatus;
        this.processed = processed;
        this.etlRunTime = etlRunTime;
        this.isManual = false;
        this.createdDate = new Date();
        this.isActive = true;
    }

    public static AccessLogEntity create(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        return new AccessLogEntity(employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime);
    }

    public static AccessLogEntity createManual(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        AccessLogEntity entity = new AccessLogEntity(employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime);
        entity.setManual(true);
        return entity;
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

    public String getLogDate() {
        return logDate;
    }

    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getInOut() {
        return inOut;
    }

    public void setInOut(String inOut) {
        this.inOut = inOut;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public Integer getProcessed() {
        return processed;
    }

    public void setProcessed(Integer processed) {
        this.processed = processed;
    }

    public Date getEtlRunTime() {
        return etlRunTime;
    }

    public void setEtlRunTime(Date etlRunTime) {
        this.etlRunTime = etlRunTime;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
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
        AccessLogEntity that = (AccessLogEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(employeeId, that.employeeId) && Objects.equals(logDate, that.logDate) && Objects.equals(logTime, that.logTime) && Objects.equals(terminalId, that.terminalId) && Objects.equals(inOut, that.inOut) && Objects.equals(readStatus, that.readStatus) && Objects.equals(processed, that.processed) && Objects.equals(etlRunTime, that.etlRunTime) && Objects.equals(isManual, that.isManual) && Objects.equals(createdDate, that.createdDate) && Objects.equals(updatedDate, that.updatedDate) && Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime, isManual, createdDate, updatedDate, isActive);
    }
}