package com.slt.peotv.lmsmangmentservice.model.dto;

import java.sql.Time;
import java.util.Date;

public class InOutDTO {
    private String employeeID;
    private Date date;
    private Date punchDate;
    private Time pucnhTime;
    private Integer InOut = 0;;
    private String terminalID;
    private AccessLogDTO accessLog;

    public boolean isMorning() {
        if (pucnhTime != null) {
            int hour = pucnhTime.toLocalTime().getHour();
            // Morning: 5 AM to 12 PM (5-11)
            return (hour >= 5 && hour < 12);
        }
        return false;
    }

    public boolean isEvening() {
        if (pucnhTime != null) {
            int hour = pucnhTime.toLocalTime().getHour();
            return (hour >= 17 && hour <= 21);
        }
        return false;
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

    public Date getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(Date punchDate) {
        this.punchDate = punchDate;
    }

    public Time getPucnhTime() {
        return pucnhTime;
    }

    public void setPucnhTime(Time pucnhTime) {
        this.pucnhTime = pucnhTime;
    }

    public Integer getInOut() {
        return InOut;
    }

    public void setInOut(Integer inOut) {
        InOut = inOut;
    }
    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public AccessLogDTO getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(AccessLogDTO accessLog) {
        this.accessLog = accessLog;
    }
}
