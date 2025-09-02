package com.slt.peotv.lmsmangmentservice.entity.Leave;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "leave_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = "publicId", name = "UK_leave_public_id"))
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LeaveEntity {

    @Column(nullable = false,unique = true)
    public String publicId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "from_date", nullable = false)
    private Date fromDate;

    @Column(name = "to_date", nullable = false)
    private Date toDate;

    @ManyToOne
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", foreignKey = @ForeignKey(name = "FK_leave_type"))
    private LeaveTypeEntity leaveType;

    @Column(name = "num_of_days")
    private Long numOfDays;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_behavior")
    private ComponentBehavior componentBehavior;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;
    
    @Builder.Default
    private Boolean notUsed = false;

    @Builder.Default
    private Boolean isManualRequest = false;
    
    private Date happenDate;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ComponetAdminsEntity> admins;
    
    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @Builder.Default
    private Date createDate = new Date();
    private Date updateDate;

    @Builder.Default
    private Boolean isEdited = false;
}
