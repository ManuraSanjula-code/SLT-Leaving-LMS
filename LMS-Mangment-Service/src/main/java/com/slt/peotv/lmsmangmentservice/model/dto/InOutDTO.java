package com.slt.peotv.lmsmangmentservice.model.dto;

import java.sql.Time;
import java.util.Date;

public class InOutDTO {
    private String employeeID;
    private Date date;
    private Date punchInMoa;
    private Date punchInEv;
    private Time timeMoa;
    private Time timeEve;
    private Integer InOut = 0;
    private Boolean isMoaning = false;
    private Boolean isEvening = false;
    private Boolean isPast = false;

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

    public Date getPunchInMoa() {
        return punchInMoa;
    }

    public void setPunchInMoa(Date punchInMoa) {
        this.punchInMoa = punchInMoa;
    }

    public Date getPunchInEv() {
        return punchInEv;
    }

    public void setPunchInEv(Date punchInEv) {
        this.punchInEv = punchInEv;
    }

    public Time getTimeMoa() {
        return timeMoa;
    }

    public void setTimeMoa(Time timeMoa) {
        this.timeMoa = timeMoa;
    }

    public Time getTimeEve() {
        return timeEve;
    }

    public void setTimeEve(Time timeEve) {
        this.timeEve = timeEve;
    }

    public Integer getInOut() {
        return InOut;
    }

    public void setInOut(Integer inOut) {
        InOut = inOut;
    }

    public Boolean getMoaning() {
        return isMoaning;
    }

    public void setMoaning(Boolean moaning) {
        isMoaning = moaning;
    }

    public Boolean getEvening() {
        return isEvening;
    }

    public void setEvening(Boolean evening) {
        isEvening = evening;
    }

    public Boolean getPast() {
        return isPast;
    }

    public void setPast(Boolean past) {
        isPast = past;
    }
}
