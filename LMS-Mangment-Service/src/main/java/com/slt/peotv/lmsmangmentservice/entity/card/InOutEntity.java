package com.slt.peotv.lmsmangmentservice.entity.card;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.InOutType;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Date;


@Entity
@Table(name = "in_out",uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "punch_time", "punch_type_time","terminalId"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private Date date;

    @Column(name = "punch_time", nullable = false)
    private Date punchTime;

    @Column(name = "punch_type_time")
    private Time punchTypeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "in_out_type", nullable = false)
    private InOutType inOutType;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "in_out_value")
    @Builder.Default
    private Integer inOutValue = -1;

    @Builder.Default
    @Column(name = "is_manual")
    private Boolean isManual = false;

    @Column(name = "etl_run_time")
    private Date etlRunTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "access_log_id")
    private AccessLogEntity accessLog;

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}