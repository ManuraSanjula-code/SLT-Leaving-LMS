package com.slt.radio.rosterservice.feign_client.model;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccessLogArchiveRest {
    private Long id;
    private String employeeID;
    private String logDate;
    private String logTime;
    private String terminalID;
    private String inOut;
    private String readStatus;
    private int processed;
    private Date etlRunTime;
}