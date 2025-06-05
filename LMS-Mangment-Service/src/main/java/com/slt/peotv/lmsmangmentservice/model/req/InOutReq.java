package com.slt.peotv.lmsmangmentservice.model.req;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Time;
import java.util.Date;

public class InOutReq {
    private Long id;
    private String employeeID;
    private Date date;
    private Date punchInMoa; // earliest moaning time -- date
    private Date punchInEv; // earliest eve time -- date
    private Time timeMoa; // earliest moaning time -- time
    private Time timeEve;// earliest eve time -- time
    private Integer InOut = 0;
    private Boolean isMoaning = false;
    private String terminalID;
    private Boolean isEvening = false;
    private Boolean isPast = false;
    private String attendanceId;
    private String accessLog;
    @JsonIgnore
    private String adminId;
    @JsonIgnore
    private String adminComment;

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

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
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

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(String accessLog) {
        this.accessLog = accessLog;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}
