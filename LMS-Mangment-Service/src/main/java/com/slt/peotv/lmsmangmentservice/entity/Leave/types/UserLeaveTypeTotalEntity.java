package com.slt.peotv.lmsmangmentservice.entity.Leave.types;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_leave_type_total")
public class UserLeaveTypeTotalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;

    @ManyToOne
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveTypeEntity leaveType;

    private Integer totalLeaves;

    public UserLeaveTypeTotalEntity() {
    }

    public UserLeaveTypeTotalEntity(Long id, String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        this.id = id;
        this.publicId = publicId;
        this.employee = employee;
        this.leaveType = leaveType;
        this.totalLeaves = totalLeaves;
    }

    public UserLeaveTypeTotalEntity(String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        this.publicId = publicId;
        this.employee = employee;
        this.leaveType = leaveType;
        this.totalLeaves = totalLeaves;
    }

    // Constructor with minimal required fields
    public UserLeaveTypeTotalEntity(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.totalLeaves = totalLeaves;
    }

    public static UserLeaveTypeTotalEntity create(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        return new UserLeaveTypeTotalEntity(employee, leaveType, totalLeaves);
    }

    public static UserLeaveTypeTotalEntity createWithPublicId(String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        return new UserLeaveTypeTotalEntity(publicId, employee, leaveType, totalLeaves);
    }

    public static UserLeaveTypeTotalEntity createWithDefaults(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer totalLeaves) {
        UserLeaveTypeTotalEntity entity = new UserLeaveTypeTotalEntity();
        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setTotalLeaves(totalLeaves);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public LeaveTypeEntity getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveTypeEntity leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getTotalLeaves() {
        return totalLeaves;
    }

    public void setTotalLeaves(Integer totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLeaveTypeTotalEntity that = (UserLeaveTypeTotalEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(leaveType, that.leaveType) &&
                Objects.equals(totalLeaves, that.totalLeaves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, employee, leaveType, totalLeaves);
    }

    @Override
    public String toString() {
        return "UserLeaveTypeTotalEntity{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", employee=" + employee +
                ", leaveType=" + leaveType +
                ", totalLeaves=" + totalLeaves +
                '}';
    }
}