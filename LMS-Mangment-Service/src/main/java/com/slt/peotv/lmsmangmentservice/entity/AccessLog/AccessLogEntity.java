package com.slt.peotv.lmsmangmentservice.entity.AccessLog;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "access_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "log_date", nullable = false)
    private String logDate;

    @Column(name = "log_time", nullable = false)
    private String logTime;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "in_out", nullable = false)
    private String inOut;

    @Column(name = "read_status", nullable = false)
    private String readStatus;

    @Column(name = "processed", nullable = false)
    private Integer processed;

    @Column(name = "etl_run_time", nullable = false)
    private Date etlRunTime;

    @Builder.Default
    @Column(name = "is_manual")
    private Boolean isManual = false;

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}