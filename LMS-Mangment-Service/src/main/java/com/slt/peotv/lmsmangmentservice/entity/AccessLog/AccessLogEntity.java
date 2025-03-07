package com.slt.peotv.lmsmangmentservice.entity.AccessLog;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "accesslog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EmployeeID", nullable = false)
    private String employeeID;

    @Column(name = "LogDate", nullable = false)
    private String logDate;

    @Column(name = "LogTime", nullable = false)
    private String logTime;

    @Column(name = "TerminalID", nullable = false)
    private String terminalID;

    @Column(name = "InOut", nullable = false)
    private String inOut;

    @Column(name = "`read`", nullable = false)  // Escaping the column name with backticks
    private String readStatus;

    @Column(name = "processed", nullable = false)
    private int processed;

    @Column(name = "etl_run_time", nullable = false)
    private Date etlRunTime;
}