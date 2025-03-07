package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveCategoryTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeRemaining;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeTotalEntity;

import java.util.List;

public interface ServiceEvent {
    public void userCreatedEvent();


    public void saveUserLeaveCategoryTotal(String cat_name, String employee_id, Integer totalLeaves);
    public List<UserLeaveCategoryTotalEntity> getUserLeaveCategoryTotal(String employee_id); // @@@
    public UserLeaveCategoryTotalEntity getUserLeaveCategoryTotal(String cat_name,String employee_id);
    public void deleteUserLeaveCategoryTotal(String publicId);


    public void saveUserLeaveTypeRemaining(String employee_id, Integer remainingLeaves, String type_name);
    public List<UserLeaveTypeRemaining> getUserLeaveTypeRemaining(String employeeID); // @@@
    public UserLeaveTypeRemaining getUserLeaveTypeRemaining(String type_name, String employee_id);
    public void deleteUserLeaveTypeRemaining(String publicId);

    public void saveUserLeaveTypeTotal(String employee_id, Integer totalLeaves, String type_name);
    public UserLeaveTypeTotalEntity getUserLeaveTypeTotal(String employee_id);
    public UserLeaveTypeTotalEntity getUserLeaveTypeTotal(String type_name, String employee_id);
    public void deleteUserLeaveTypeTotal(String publicId);
}
