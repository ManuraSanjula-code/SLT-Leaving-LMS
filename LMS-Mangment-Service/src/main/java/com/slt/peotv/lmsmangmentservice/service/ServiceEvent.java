package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveCategoryTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;

import java.util.List;

public interface ServiceEvent {
    public List<UserLeaveTypeRemainingEntity> getUserLeaveTypeRemaining(String employeeID); // @@@
    public UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String type_name, String employee_id);
}
