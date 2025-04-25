package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLeaveTypeRemainingRepo extends CrudRepository<UserLeaveTypeRemainingEntity, Long> {
    Optional<UserLeaveTypeRemainingEntity> findByPublicId(String publicId);
    List<UserLeaveTypeRemainingEntity> findUserLeaveTypeRemainingByEmployeeIDAndLeaveType(String employeeID, LeaveTypeEntity leaveTypeEntity);
    List<UserLeaveTypeRemainingEntity> findUserLeaveTypeRemainingByEmployeeID(String employeeID);
    UserLeaveTypeRemainingEntity findByEmployeeIDAndLeaveType(String employeeId, LeaveTypeEntity leaveType);
    List<UserLeaveTypeRemainingEntity> findByEmployeeID(String employeeId);
}
