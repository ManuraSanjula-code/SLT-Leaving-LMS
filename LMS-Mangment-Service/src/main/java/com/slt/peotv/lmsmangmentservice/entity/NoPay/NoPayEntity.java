package com.slt.peotv.lmsmangmentservice.entity.NoPay;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@Table(name = "nopay")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoPayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public String publicId;

    private String employeeID;

    @Column(name = "submission_date")
    private Date submissionDate;

    @Column(name = "actual_date")
    private Date acctualDate;

    @Builder.Default
    private Boolean isHalfDay = false; ///  To indicate half-day ---- because user is half-day he/she can make absent or leave or make movement

    @Builder.Default
    private Boolean unSuccessful = false; /// To indicate unSuccessful ---- because user is unSuccessful he/she can make absent or leave or make movement

    @Builder.Default
    private Boolean isLate = false; /// To indicate isLate ---- because user is isLate he/she can make absent or leave or make movement

    @Builder.Default
    private Boolean isLateCover = false; /// To indicate latecover---- because user is latecover he/she can make absent or leave or make movement

    @Builder.Default
    private Boolean isAbsent = false; /// To indicate isAbsent ---- because user is isAbsent he/she can make absent or leave or make movement

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    private String comment;

    private Date happenDate;
}
