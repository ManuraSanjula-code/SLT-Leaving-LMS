package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Leave.category.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveCategoryTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ServiceEventImpl implements ServiceEvent {
    @Autowired
    private UserLeaveCategoryTotalRepo userLeaveCategoryTotalRepo;
    @Autowired
    private UserLeaveTypeTotalRepo userLeaveTypeTotalRepo;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private LeaveCategoryRepo leaveCategoryRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepo;
    @Autowired
    private LMS_Service lmsService;
    @Autowired
    private Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<UserLeaveTypeRemainingEntity> getUserLeaveTypeRemaining(String employee_id) {
        return userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployeeID(employee_id);
    }

    @Override
    public UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String type_name, String employee_id) {
        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);
        if(employee_id != null && byName.isPresent()) {
            List<UserLeaveTypeRemainingEntity> byLeaveTypeAndUser = userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployeeIDAndLeaveType(employee_id,byName.get());
            if(byLeaveTypeAndUser.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            else
                return byLeaveTypeAndUser.get(0);
        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

}
