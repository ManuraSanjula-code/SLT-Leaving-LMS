package com.slt.peotv.lmsmangmentservice.entity.Movement;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import lombok.*;

@Entity
@Table(name = "movements")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;
    private String employeeID;

    @Column(name = "In_Time", length = 45)
    private String inTime;

    @Column(name = "Out_Time", length = 45)
    private String outTime;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Log_Time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;

    @Column(name = "Sup_app_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date supAppTime;

    @Column(name = "Man_App_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date manAppTime;

    @Column(name = "HOD_App_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hodAppTime;

    @Column(name = "category", length = 45)
    private String category;

    @Column(name = "Destination", length = 45)
    private String destination;

    @Column(name = "Employee_ID", length = 45)
    private String employeeId;

    @Column(name = "REQ_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reqDate;

    private String hod;
    private String supervisor;

    @Enumerated(EnumType.STRING)  // FIXED: Enum mapping
    private MovementType movementType;

    @Column(name = "ATT_SYNC")
    @Builder.Default
    private Integer attSync = 0;

    private Date happenDate; /// The Day situation happened to make a movement to resolve it

    @Builder.Default
    private Boolean isPending = false;
    @Builder.Default
    private Boolean isAccepted = false;
    @Builder.Default
    private Boolean isExpired = false;
    @Builder.Default
    private Boolean isHalfDay = false;
    @Builder.Default
    private Boolean isLate = false;
    @Builder.Default
    private Boolean isAbsent = false;
    @Builder.Default
    private Boolean isUnSuccessfulAttdate = false;
    @Builder.Default
    private Boolean isLateCover = false;
    @Builder.Default
    private Boolean unAuthorized = false;

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;
}