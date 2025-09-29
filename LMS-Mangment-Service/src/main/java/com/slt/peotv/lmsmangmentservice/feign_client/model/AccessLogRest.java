package com.slt.peotv.lmsmangmentservice.feign_client.model;

import java.util.Date;
import java.util.Objects;

public class AccessLogRest {
    private String employeeId;
    private String logDate;
    private String logTime;
    private String terminalId;
    private String inOut;
    private String readStatus;
    private Integer processed;
    private Date etlRunTime;
    private Boolean isManual = false;
    private Date createdDate = new Date();
    private Date updatedDate;
    private Boolean isActive = true;

    public AccessLogRest() {
    }

    public AccessLogRest(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime, Boolean isManual, Date createdDate, Date updatedDate, Boolean isActive) {
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

    public AccessLogRest(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
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

    public static AccessLogRest create(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        return new AccessLogRest(employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime);
    }

    public static AccessLogRest createManual(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        AccessLogRest rest = new AccessLogRest(employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime);
        rest.setIsManual(true);
        return rest;
    }

    public static AccessLogRest createWithDefaults(String employeeId, String logDate, String logTime, String terminalId, String inOut, String readStatus, Integer processed, Date etlRunTime) {
        AccessLogRest rest = new AccessLogRest();
        rest.setEmployeeId(employeeId);
        rest.setLogDate(logDate);
        rest.setLogTime(logTime);
        rest.setTerminalId(terminalId);
        rest.setInOut(inOut);
        rest.setReadStatus(readStatus);
        rest.setProcessed(processed);
        rest.setEtlRunTime(etlRunTime);
        rest.setIsManual(false);
        rest.setCreatedDate(new Date());
        rest.setIsActive(true);
        return rest;
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

    public Boolean getIsManual() {
        return isManual;
    }

    public void setIsManual(Boolean isManual) {
        this.isManual = isManual;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessLogRest that = (AccessLogRest) o;
        return Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(logDate, that.logDate) &&
                Objects.equals(logTime, that.logTime) &&
                Objects.equals(terminalId, that.terminalId) &&
                Objects.equals(inOut, that.inOut) &&
                Objects.equals(readStatus, that.readStatus) &&
                Objects.equals(processed, that.processed) &&
                Objects.equals(etlRunTime, that.etlRunTime) &&
                Objects.equals(isManual, that.isManual) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(updatedDate, that.updatedDate) &&
                Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, logDate, logTime, terminalId, inOut, readStatus, processed, etlRunTime, isManual, createdDate, updatedDate, isActive);
    }

    @Override
    public String toString() {
        return "AccessLogRest{" +
                "employeeId='" + employeeId + '\'' +
                ", logDate='" + logDate + '\'' +
                ", logTime='" + logTime + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", inOut='" + inOut + '\'' +
                ", readStatus='" + readStatus + '\'' +
                ", processed=" + processed +
                ", etlRunTime=" + etlRunTime +
                ", isManual=" + isManual +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", isActive=" + isActive +
                '}';
    }
}