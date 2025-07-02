package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.PayStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ResolveType;
import jakarta.persistence.*;
import lombok.*;
import java.sql.Time;
import java.util.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "attendance",uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "date","arrival_date","arrival_time"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(nullable = false)
    private Date date;

    @Column(name = "arrival_date")
    private Date arrivalDate;

    @Column(name = "arrival_time")
    private Time arrivalTime;

    private Time leftTime;

    @Column(name = "terminal_id", nullable = false)
    @Builder.Default
    private String terminalId = "NONE";

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type")
    private AttendanceType attendanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatus leaveStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status")
    private PayStatus payStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolve")
    private ResolveType resolve;

    @Builder.Default
    private Boolean isLate = false;
    @Builder.Default
    private Boolean isLateCovered = false;
    @Builder.Default
    private Boolean isUnauthorized = false;
    @Builder.Default
    private Boolean isUnSuccessful = false;
    @Builder.Default
    private Boolean isHoliday = false;
    @Builder.Default
    private Boolean isResolved = false;
    @Builder.Default
    private Boolean hasIssues = false;
    @Builder.Default
    private Boolean isManual = false;
    @Column(name = "issue_description", length = 1000)
    private String issueDescription;

    @Column(name = "due_date_for_ua")
    private Date dueDateForUA;

    @Column(name = "etl_run_time")
    private Date etlRunTime;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate = new Date();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    private Boolean viaMovement;
    private Boolean viaLeave;

    public Boolean getIsFullDay() {
        return attendanceType != null && attendanceType.equals(AttendanceType.FULL_DAY);
    }


    public Boolean isArrivalOnWeekend() {
        if (this.arrivalDate == null) {
            return false;
        }
        
        LocalDate localArrivalDate = this.arrivalDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        
        DayOfWeek dayOfWeek = localArrivalDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}