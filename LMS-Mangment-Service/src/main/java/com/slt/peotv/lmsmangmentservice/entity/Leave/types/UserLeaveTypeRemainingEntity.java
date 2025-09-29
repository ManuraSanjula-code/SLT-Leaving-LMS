package com.slt.peotv.lmsmangmentservice.entity.Leave.types;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_leave_type_remaining")
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

    public UserLeaveTypeRemainingEntity() {
    }

    public UserLeaveTypeRemainingEntity(Long id, String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        this.id = id;
        this.publicId = publicId;
        this.employee = employee;
        this.leaveType = leaveType;
        this.remainingLeaves = remainingLeaves;
    }

    public UserLeaveTypeRemainingEntity(String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        this.publicId = publicId;
        this.employee = employee;
        this.leaveType = leaveType;
        this.remainingLeaves = remainingLeaves;
    }

    public UserLeaveTypeRemainingEntity(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.remainingLeaves = remainingLeaves;
    }

    public static UserLeaveTypeRemainingEntity create(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        return new UserLeaveTypeRemainingEntity(employee, leaveType, remainingLeaves);
    }

    public static UserLeaveTypeRemainingEntity createWithPublicId(String publicId, EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        return new UserLeaveTypeRemainingEntity(publicId, employee, leaveType, remainingLeaves);
    }

    public static UserLeaveTypeRemainingEntity createWithDefaults(EmployeeEntity employee, LeaveTypeEntity leaveType, Integer remainingLeaves) {
        UserLeaveTypeRemainingEntity entity = new UserLeaveTypeRemainingEntity();
        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setRemainingLeaves(remainingLeaves);
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

    public Integer getRemainingLeaves() {
        return remainingLeaves;
    }

    public void setRemainingLeaves(Integer remainingLeaves) {
        this.remainingLeaves = remainingLeaves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLeaveTypeRemainingEntity that = (UserLeaveTypeRemainingEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(leaveType, that.leaveType) &&
                Objects.equals(remainingLeaves, that.remainingLeaves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, employee, leaveType, remainingLeaves);
    }

    @Override
    public String toString() {
        return "UserLeaveTypeRemainingEntity{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", employee=" + employee +
                ", leaveType=" + leaveType +
                ", remainingLeaves=" + remainingLeaves +
                '}';
    }
}