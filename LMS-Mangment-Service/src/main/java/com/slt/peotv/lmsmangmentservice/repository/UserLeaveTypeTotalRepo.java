package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLeaveTypeTotalRepo extends CrudRepository<UserLeaveTypeTotalEntity, Long> {
    Optional<UserLeaveTypeTotalEntity> findByPublicId(String publicId);
    List<UserLeaveTypeTotalEntity> findUserLeaveTypeTotalByEmployeeAndLeaveType(EmployeeEntity employee, LeaveTypeEntity leaveTypeEntity);
    UserLeaveTypeTotalEntity findByEmployeeAndLeaveType(EmployeeEntity employee, LeaveTypeEntity leaveType);
    List<UserLeaveTypeTotalEntity> findByEmployee(EmployeeEntity employee);
}
