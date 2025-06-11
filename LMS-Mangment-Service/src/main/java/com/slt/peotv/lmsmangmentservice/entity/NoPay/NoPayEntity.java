package com.slt.peotv.lmsmangmentservice.entity.NoPay;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "no_pay")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoPayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Column(name = "submission_date", nullable = false)
    private Date submissionDate;

    @Column(name = "actual_date", nullable = false)
    private Date date;

    @Column(length = 1000)
    private String comment;

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}