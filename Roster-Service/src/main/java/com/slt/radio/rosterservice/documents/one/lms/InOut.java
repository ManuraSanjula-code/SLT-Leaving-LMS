package com.slt.radio.rosterservice.documents.one.lms;

import com.slt.radio.rosterservice.documents.enums.InOutType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;

@Document(collection = "in_outs")
@CompoundIndexes({
        @CompoundIndex(
                name = "unique_inout_record",
                def = "{'employeeID': 1, 'punchTime': 1, 'punchInMoa': 1, 'punchInEv': 1}",
                unique = true
        )
})
public class InOut {
    @Id
    private String id;
    private String employeeId;

    private Date date;
    private Date punchTime;
    private LocalTime punchTypeTime;

    private String terminalId;
    private Integer inOutValue = -1;
    private Boolean isManual = false;

    private InOutType inOutType;

    private Date createdDate = new Date();
    private Date updatedDate;
    private Boolean isActive = true;
    private Date etlRunTime;

    public InOut() {}

    public InOut(String id, String employeeId, Date date, Date punchTime, LocalTime punchTypeTime,
                 String terminalId, Integer inOutValue, Boolean isManual, InOutType inOutType,
                 Date createdDate, Date updatedDate, Boolean isActive, Date etlRunTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.date = date;
        this.punchTime = punchTime;
        this.punchTypeTime = punchTypeTime;
        this.terminalId = terminalId;
        this.inOutValue = inOutValue != null ? inOutValue : -1;
        this.isManual = isManual != null ? isManual : false;
        this.inOutType = inOutType;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate;
        this.isActive = isActive != null ? isActive : true;
        this.etlRunTime = etlRunTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getPunchTime() { return punchTime; }
    public void setPunchTime(Date punchTime) { this.punchTime = punchTime; }

    public LocalTime getPunchTypeTime() { return punchTypeTime; }
    public void setPunchTypeTime(LocalTime punchTypeTime) { this.punchTypeTime = punchTypeTime; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public Integer getInOutValue() { return inOutValue; }
    public void setInOutValue(Integer inOutValue) { this.inOutValue = inOutValue; }

    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }

    public InOutType getInOutType() { return inOutType; }
    public void setInOutType(InOutType inOutType) { this.inOutType = inOutType; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(Date updatedDate) { this.updatedDate = updatedDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Date getEtlRunTime() { return etlRunTime; }
    public void setEtlRunTime(Date etlRunTime) { this.etlRunTime = etlRunTime; }

    @Override
    public String toString() {
        return "InOut{" +
                "id='" + id + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", date=" + date +
                ", punchTime=" + punchTime +
                ", punchTypeTime=" + punchTypeTime +
                ", terminalId='" + terminalId + '\'' +
                ", inOutValue=" + inOutValue +
                ", isManual=" + isManual +
                ", inOutType=" + inOutType +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", isActive=" + isActive +
                ", etlRunTime=" + etlRunTime +
                '}';
    }

    // Builder pattern
    public static InOutBuilder builder() {
        return new InOutBuilder();
    }

    public static class InOutBuilder {
        private String id;
        private String employeeId;
        private Date date;
        private Date punchTime;
        private LocalTime punchTypeTime;
        private String terminalId;
        private Integer inOutValue = -1;
        private Boolean isManual = false;
        private InOutType inOutType;
        private Date createdDate = new Date();
        private Date updatedDate;
        private Boolean isActive = true;
        private Date etlRunTime;

        public InOutBuilder id(String id) { this.id = id; return this; }
        public InOutBuilder employeeId(String employeeId) { this.employeeId = employeeId; return this; }
        public InOutBuilder date(Date date) { this.date = date; return this; }
        public InOutBuilder punchTime(Date punchTime) { this.punchTime = punchTime; return this; }
        public InOutBuilder punchTypeTime(LocalTime punchTypeTime) { this.punchTypeTime = punchTypeTime; return this; }
        public InOutBuilder terminalId(String terminalId) { this.terminalId = terminalId; return this; }
        public InOutBuilder inOutValue(Integer inOutValue) { this.inOutValue = inOutValue; return this; }
        public InOutBuilder isManual(Boolean isManual) { this.isManual = isManual; return this; }
        public InOutBuilder inOutType(InOutType inOutType) { this.inOutType = inOutType; return this; }
        public InOutBuilder createdDate(Date createdDate) { this.createdDate = createdDate; return this; }
        public InOutBuilder updatedDate(Date updatedDate) { this.updatedDate = updatedDate; return this; }
        public InOutBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public InOutBuilder etlRunTime(Date etlRunTime) { this.etlRunTime = etlRunTime; return this; }

        public InOut build() {
            return new InOut(id, employeeId, date, punchTime, punchTypeTime, terminalId,
                    inOutValue, isManual, inOutType, createdDate, updatedDate,
                    isActive, etlRunTime);
        }
    }
}
