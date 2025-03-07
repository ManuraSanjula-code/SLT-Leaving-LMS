package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.sql.Time;
import java.util.Date;
import lombok.*;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false)
    private Date date;

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

    @Column(length = 1000)
    private String issueDescription;

    private Date dueDateForUA;

}