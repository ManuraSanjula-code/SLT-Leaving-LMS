package com.slt.radio.rosterservice.document.one.lms;

import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
            this.isManual = archiveRest.getIsManual();
            this.createdDate = archiveRest.getCreatedDate();
            this.updatedDate = archiveRest.getUpdatedDate();
            this.isActive = archiveRest.getIsActive();
        }
    }
}
