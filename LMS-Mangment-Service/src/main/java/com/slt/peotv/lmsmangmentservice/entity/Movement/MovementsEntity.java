package com.slt.peotv.lmsmangmentservice.entity.Movement;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "movements")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MovementsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;

    @Column(name = "In_Time", length = 45)
    private Time inTime;

    @Column(name = "Out_Time", length = 45)
    private Time outTime;

    private String inTimeRaw;
    private String outTimeRaw;
    private String happenDateRaw;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Log_Time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;

    @Column(name = "category", length = 45)
    private String category;

    @Column(name = "Destination", length = 45)
    private String destination;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(name = "REQ_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reqDate;

    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "ATT_SYNC")
    @Builder.Default
    private Integer attSync = 0;

    private Date happenDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ComponetAdminsEntity> admins;

    @Builder.Default
    private Date createDate = new Date();
    private Date updateDate;

    @Builder.Default
    private Boolean isEdited = false;

}