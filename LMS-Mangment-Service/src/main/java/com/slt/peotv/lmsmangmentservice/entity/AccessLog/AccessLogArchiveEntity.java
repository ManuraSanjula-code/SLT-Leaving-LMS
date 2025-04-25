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
@ToString
public class AccessLogArchiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EmployeeID")
    private String employeeID;

    @Column(name = "LogDate")
    private String logDate;

    private String logTime;

    private String terminalID;

    private String inOut;

    @Column(name = "`read`")  // Escaping the column name with backticks
    private String readStatus;

    private int processed;

    private Date etlRunTime;
}