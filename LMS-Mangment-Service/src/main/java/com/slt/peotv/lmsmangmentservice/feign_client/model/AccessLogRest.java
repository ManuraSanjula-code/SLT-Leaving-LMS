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
    @Builder.Default
    private Boolean isManual = false;
    @Builder.Default
    private Date createdDate = new Date();
    private Date updatedDate;
    @Builder.Default
    private Boolean isActive = true;
}