package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;

import java.util.List;

public interface ServiceEvent {
    public List<UserLeaveTypeRemainingEntity> getUserLeaveTypeRemaining(String employeeID); // @@@
    public UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String type_name, String employee_id);
}
