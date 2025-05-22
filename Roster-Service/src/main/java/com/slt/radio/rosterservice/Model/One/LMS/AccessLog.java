package com.slt.radio.rosterservice.Model.One.LMS;

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

    private String employeeID;
    private String logDate;
    private String logTime;
    private String terminalID;
    private String inOut;
    private String readStatus;
    private int processed;
    private Date etlRunTime;
    private Date createdAt;

    public AccessLog(AccessLogArchiveRest archiveRest) {
        this.id = String.valueOf(archiveRest.getId());
        this.employeeID = archiveRest.getEmployeeID();
        this.logDate = archiveRest.getLogDate();
        this.logTime = archiveRest.getLogTime();
        this.terminalID = archiveRest.getTerminalID();
        this.inOut = archiveRest.getInOut().trim();
        this.readStatus = archiveRest.getReadStatus();
        this.processed = archiveRest.getProcessed();
        this.etlRunTime = archiveRest.getEtlRunTime();
        this.createdAt = new Date();
    }
}
