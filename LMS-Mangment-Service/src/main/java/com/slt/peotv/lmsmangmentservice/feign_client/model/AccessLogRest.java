package com.slt.peotv.lmsmangmentservice.feign_client.model;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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
}