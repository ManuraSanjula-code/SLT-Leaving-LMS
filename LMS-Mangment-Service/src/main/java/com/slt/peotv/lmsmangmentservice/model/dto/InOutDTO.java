package com.slt.peotv.lmsmangmentservice.model.dto;

import com.slt.peotv.lmsmangmentservice.entity.Enum.InOutType;

import java.sql.Time;
import java.util.Date;

public class InOutDTO {
    private Long id;
    private String employeeID;
    private Date date;
    private Date punchTime;
    private Time punchTypeTime;
    private InOutType inOutType;
    private String terminalID;
    private Integer inOutValue = -1;
    private Boolean isManual = false;
    private Date etlRunTime;
    private Date createdDate;
    private Date updatedDate;
    private Boolean isActive = true;
    private AccessLogDTO accessLog;

    public boolean isMorning() {
        if (punchTypeTime != null) {
            int hour = punchTypeTime.toLocalTime().getHour();
            return (hour >= 5 && hour < 12);
        }
        return false;
    }

    public boolean isEvening() {
        if (punchTypeTime != null) {
            int hour = punchTypeTime.toLocalTime().getHour();
            return (hour >= 17 && hour <= 21);
        }
        return false;
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

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public Integer getInOutValue() {
        return inOutValue;
    }

    public void setInOutValue(Integer inOutValue) {
        this.inOutValue = inOutValue;
    }

    public Boolean getIsManual() {
        return isManual;
    }

    public void setIsManual(Boolean isManual) {
        this.isManual = isManual;
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

    public AccessLogDTO getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(AccessLogDTO accessLog) {
        this.accessLog = accessLog;
    }
}