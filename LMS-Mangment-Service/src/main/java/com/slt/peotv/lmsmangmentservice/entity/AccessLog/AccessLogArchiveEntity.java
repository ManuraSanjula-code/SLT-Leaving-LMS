package com.slt.peotv.lmsmangmentservice.entity.AccessLog;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "accesslog_archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLogArchiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String employeeID;

    private String logDate;

    private String logTime;

    private String terminalID;

    private String inOut;

    @Column(name = "`read`")  // Escaping the column name with backticks
    private String readStatus;

    private int processed;

    private Date etlRunTime;
}