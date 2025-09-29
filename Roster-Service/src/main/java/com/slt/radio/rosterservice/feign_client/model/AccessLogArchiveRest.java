package com.slt.radio.rosterservice.feign_client.model;

import java.util.Date;

public class AccessLogArchiveRest {
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

    public AccessLogArchiveRest() {}

    public AccessLogArchiveRest(String employeeId, String logDate, String logTime, String terminalId,
                                String inOut, String readStatus, Integer processed, Date etlRunTime,
                                Boolean isManual, Date createdDate, Date updatedDate, Boolean isActive) {
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
    public String toString() {
        return "AccessLogArchiveRest{" +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessLogArchiveRest that = (AccessLogArchiveRest) o;
        return java.util.Objects.equals(employeeId, that.employeeId) &&
                java.util.Objects.equals(logDate, that.logDate) &&
                java.util.Objects.equals(logTime, that.logTime) &&
                java.util.Objects.equals(terminalId, that.terminalId) &&
                java.util.Objects.equals(inOut, that.inOut) &&
                java.util.Objects.equals(readStatus, that.readStatus) &&
                java.util.Objects.equals(processed, that.processed) &&
                java.util.Objects.equals(etlRunTime, that.etlRunTime) &&
                java.util.Objects.equals(isManual, that.isManual) &&
                java.util.Objects.equals(createdDate, that.createdDate) &&
                java.util.Objects.equals(updatedDate, that.updatedDate) &&
                java.util.Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(employeeId, logDate, logTime, terminalId, inOut,
                readStatus, processed, etlRunTime, isManual,
                createdDate, updatedDate, isActive);
    }

    // Builder pattern
    public static AccessLogArchiveRestBuilder builder() {
        return new AccessLogArchiveRestBuilder();
    }

    public static class AccessLogArchiveRestBuilder {
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

        public AccessLogArchiveRestBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public AccessLogArchiveRestBuilder logDate(String logDate) {
            this.logDate = logDate;
            return this;
        }

        public AccessLogArchiveRestBuilder logTime(String logTime) {
            this.logTime = logTime;
            return this;
        }

        public AccessLogArchiveRestBuilder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public AccessLogArchiveRestBuilder inOut(String inOut) {
            this.inOut = inOut;
            return this;
        }

        public AccessLogArchiveRestBuilder readStatus(String readStatus) {
            this.readStatus = readStatus;
            return this;
        }

        public AccessLogArchiveRestBuilder processed(Integer processed) {
            this.processed = processed;
            return this;
        }

        public AccessLogArchiveRestBuilder etlRunTime(Date etlRunTime) {
            this.etlRunTime = etlRunTime;
            return this;
        }

        public AccessLogArchiveRestBuilder isManual(Boolean isManual) {
            this.isManual = isManual;
            return this;
        }

        public AccessLogArchiveRestBuilder createdDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public AccessLogArchiveRestBuilder updatedDate(Date updatedDate) {
            this.updatedDate = updatedDate;
            return this;
        }

        public AccessLogArchiveRestBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AccessLogArchiveRest build() {
            return new AccessLogArchiveRest(employeeId, logDate, logTime, terminalId, inOut,
                    readStatus, processed, etlRunTime, isManual,
                    createdDate, updatedDate, isActive);
        }
    }
}