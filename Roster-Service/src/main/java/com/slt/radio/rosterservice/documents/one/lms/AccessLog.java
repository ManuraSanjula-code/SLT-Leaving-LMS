package com.slt.radio.rosterservice.documents.one.lms;

import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "access_logs")
public class AccessLog {
    @Id
    private String id;
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

    // Default constructor
    public AccessLog() {}

    // All-args constructor
    public AccessLog(String id, String employeeId, String logDate, String logTime,
                     String terminalId, String inOut, String readStatus, Integer processed,
                     Date etlRunTime, Boolean isManual, Date createdDate, Date updatedDate,
                     Boolean isActive) {
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

    // Constructor with AccessLogArchiveRest (preserved from original)
    public AccessLog(AccessLogArchiveRest archiveRest) {
        if (archiveRest != null) {
            this.employeeId = archiveRest.getEmployeeId();
            this.logDate = archiveRest.getLogDate();
            this.logTime = archiveRest.getLogTime();
            this.terminalId = archiveRest.getTerminalId();
            this.inOut = archiveRest.getInOut();
            this.readStatus = archiveRest.getReadStatus();
            this.processed = archiveRest.getProcessed();
            this.etlRunTime = archiveRest.getEtlRunTime();
            this.isManual = archiveRest.getIsManual() != null ? archiveRest.getIsManual() : false;
            this.createdDate = archiveRest.getCreatedDate() != null ? archiveRest.getCreatedDate() : new Date();
            this.updatedDate = archiveRest.getUpdatedDate();
            this.isActive = archiveRest.getIsActive() != null ? archiveRest.getIsActive() : true;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    // Builder pattern
    public static AccessLogBuilder builder() {
        return new AccessLogBuilder();
    }

    public static class AccessLogBuilder {
        private String id;
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

        public AccessLogBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AccessLogBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public AccessLogBuilder logDate(String logDate) {
            this.logDate = logDate;
            return this;
        }

        public AccessLogBuilder logTime(String logTime) {
            this.logTime = logTime;
            return this;
        }

        public AccessLogBuilder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public AccessLogBuilder inOut(String inOut) {
            this.inOut = inOut;
            return this;
        }

        public AccessLogBuilder readStatus(String readStatus) {
            this.readStatus = readStatus;
            return this;
        }

        public AccessLogBuilder processed(Integer processed) {
            this.processed = processed;
            return this;
        }

        public AccessLogBuilder etlRunTime(Date etlRunTime) {
            this.etlRunTime = etlRunTime;
            return this;
        }

        public AccessLogBuilder isManual(Boolean isManual) {
            this.isManual = isManual;
            return this;
        }

        public AccessLogBuilder createdDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public AccessLogBuilder updatedDate(Date updatedDate) {
            this.updatedDate = updatedDate;
            return this;
        }

        public AccessLogBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AccessLog build() {
            return new AccessLog(id, employeeId, logDate, logTime, terminalId, inOut,
                    readStatus, processed, etlRunTime, isManual, createdDate,
                    updatedDate, isActive);
        }
    }
}