package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import com.slt.peotv.lmsmangmentservice.entity.EditedBy;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"inOuts", "editedBys"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"inOuts", "editedBys"})
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false)
    private Date date;
    private Date etl_run_time;

    private String employeeID;
    @Builder.Default
    private Boolean isFullDay = false;
    private Date arrivalDate;
    private Time arrivalTime;
    private Time leftTime;

    @Builder.Default
    private Boolean isLate = false;

    @Builder.Default
    private Boolean lateCover = false;

    @Builder.Default
    private Boolean isHalfDay = false;

    @Builder.Default
    private Boolean isFullLeave = false;

    @Builder.Default
    private Boolean isShortLeave = false;

    @Builder.Default
    private Boolean isAbsent = false;

    @Builder.Default
    private Boolean isUnSuccessful = false;

    @Builder.Default
    private Boolean isNoPay = false;

    @Builder.Default
    private Boolean issues = false;

    @Builder.Default
    private Boolean isUnAuthorized = false;

    @Builder.Default
    private Boolean resolve = false;

    @Builder.Default
    private Boolean leaveSuccess = false;

    @Builder.Default
    private Boolean leaveReq = false;

    @Column(length = 1000)
    private String issueDescription;

    private Date dueDateForUA;

    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean nopay = false;

    private String userId;
    
    @Builder.Default
    private Boolean viaMovement = false;
    
    @Builder.Default
    private Boolean viaLeave = false;

    @Builder.Default
    private Boolean isManual = false;

    @Column(name = "TerminalID", nullable = false)
    private String terminalID;

    @Builder.Default
    private Boolean isHoliday = false;

    @OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<InOutEntity> inOuts = new ArrayList<>();

    @OneToMany
    @Builder.Default
    private List<EditedBy> editedBys = new ArrayList<>();

    @Builder.Default
    private Date createDate = new Date();
    private Date updateDate;

    @Builder.Default
    private Boolean isEdited = false;

}