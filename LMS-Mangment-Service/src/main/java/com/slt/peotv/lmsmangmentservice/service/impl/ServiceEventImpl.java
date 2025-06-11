package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ServiceEventImpl implements ServiceEvent {
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepo;
    @Autowired
    private Helper helper;

    @Override
    public List<UserLeaveTypeRemainingEntity> getUserLeaveTypeRemaining(String employee_id) {
        return userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployee(helper.getEmployeeById(employee_id));
    }

    @Override
    public UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String type_name, String employee_id) {

        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);
        if (employee_id != null && byName.isPresent()) {
            List<UserLeaveTypeRemainingEntity> byLeaveTypeAndUser = userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployeeAndLeaveType(helper.getEmployeeById(employee_id), byName.get());
            if (byLeaveTypeAndUser.isEmpty()) {
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            } else {
                return byLeaveTypeAndUser.get(0);
            }
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

}
