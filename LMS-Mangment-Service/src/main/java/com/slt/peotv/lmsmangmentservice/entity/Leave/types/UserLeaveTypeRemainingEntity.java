package com.slt.peotv.lmsmangmentservice.entity.Leave.types;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_leave_type_remaining")
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserLeaveTypeRemainingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;

    @ManyToOne
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveTypeEntity leaveType;

    private Integer remainingLeaves;
}


