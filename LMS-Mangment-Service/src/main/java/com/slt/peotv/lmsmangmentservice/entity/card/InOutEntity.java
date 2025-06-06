package com.slt.peotv.lmsmangmentservice.entity.card;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.EditedBy;
import jakarta.persistence.*;
import lombok.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "InOut", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employeeID", "date", "punchInMoa", "punchInEv", "timeMoa", "timeEve"})
})
@EqualsAndHashCode(exclude = {"attendance", "accessLog", "editedBys"}) // Exclude lazy-loaded properties
public class InOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String employeeID;
    private Date date;
    private Date punchInMoa;
    private Date punchInEv;
    private Time timeMoa;
    private Time timeEve;

    @Builder.Default
    private Integer InOut = 0;
    @Builder.Default
    private Boolean isMoaning = false;

    @Column(name = "TerminalID", nullable = false)
    private String terminalID;

    @Builder.Default
    private Boolean isEvening = false;
    @Builder.Default
    private Boolean isPast = false;

    private Date etlRunTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "access_log_id")
    private AccessLogEntity accessLog;

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
