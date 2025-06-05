package com.slt.peotv.lmsmangmentservice.entity.AccessLog;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EditedBy;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "accesslog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    @Column(name = "`read`", nullable = false)
    private String readStatus;

    @Column(name = "processed", nullable = false)
    private int processed;

    @Column(name = "etl_run_time", nullable = false)
    private Date etlRunTime;

    @OneToOne(mappedBy = "accessLog", fetch = FetchType.EAGER)
    private InOutEntity inOu;

    @OneToMany
    @Builder.Default
    private List<EditedBy> editedBys = new ArrayList<>();

    @Builder.Default
    private Date createDate = new Date();
    private Date updateDate;

    @Builder.Default
    private Boolean isEdited = false;
    @Builder.Default
    private Boolean isManual = false;
}