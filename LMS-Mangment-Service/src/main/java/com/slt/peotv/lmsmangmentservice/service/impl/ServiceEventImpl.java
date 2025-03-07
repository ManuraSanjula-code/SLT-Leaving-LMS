package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveCategoryTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeRemaining;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
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
    public void userCreatedEvent() {

    }

    @Override
    public void saveUserLeaveCategoryTotal(String cat_name,String employee_id, Integer totalLeaves) {

        Optional<LeaveCategoryEntity> byName = leaveCategoryRepo.findByName(cat_name);

        if(employee_id != null && byName.isPresent()) {
            LeaveCategoryEntity leaveCategoryEntity = byName.get();
            UserLeaveCategoryTotalEntity userLeaveCategoryTotalEntity = new UserLeaveCategoryTotalEntity();
            userLeaveCategoryTotalEntity.setPublicId(utils.generateId(10));
            userLeaveCategoryTotalEntity.setLeaveCategory(leaveCategoryEntity);
            userLeaveCategoryTotalEntity.setEmployeeID(employee_id);
            userLeaveCategoryTotalRepo.save(userLeaveCategoryTotalEntity);
        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public List<UserLeaveCategoryTotalEntity> getUserLeaveCategoryTotal(String employee_id) {
        if(employee_id != null) return userLeaveCategoryTotalRepo.findByEmployeeID(employee_id);
        else throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }


    @Override
    public UserLeaveCategoryTotalEntity getUserLeaveCategoryTotal(String cat_name,String employee_id) {

        Optional<LeaveCategoryEntity> byName = leaveCategoryRepo.findByName(cat_name);
        if(employee_id != null && byName.isPresent()) {
            List<UserLeaveCategoryTotalEntity> byLeaveCategoryAndUser = userLeaveCategoryTotalRepo.findByLeaveCategoryAndEmployeeID(byName.get(), employee_id);
            if(byLeaveCategoryAndUser.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            else
                return byLeaveCategoryAndUser.get(0);
        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteUserLeaveCategoryTotal(String publicId) {
        Optional<UserLeaveCategoryTotalEntity> byPublicId = userLeaveCategoryTotalRepo.findByPublicId(publicId);
        if(byPublicId.isPresent())
            userLeaveCategoryTotalRepo.delete(byPublicId.get());
        else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void saveUserLeaveTypeRemaining(String employee_id, Integer remainingLeaves, String type_name) {
        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);

        if(byName.isPresent() && employee_id != null) {
            LeaveTypeEntity leaveCategoryEntity = byName.get();

            UserLeaveTypeRemaining userLeaveTypeRemaining = new UserLeaveTypeRemaining();
            userLeaveTypeRemaining.setPublicId(utils.generateId(10));
            userLeaveTypeRemaining.setLeaveType(leaveCategoryEntity);
            userLeaveTypeRemaining.setRemainingLeaves(remainingLeaves);
            userLeaveTypeRemaining.setEmployeeID(employee_id);

            userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);

        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }


    @Override
    public List<UserLeaveTypeRemaining> getUserLeaveTypeRemaining(String employee_id) {
        return userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployeeID(employee_id);
    }

    @Override
    public UserLeaveTypeRemaining getUserLeaveTypeRemaining(String type_name,String employee_id) {
        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);
        if(employee_id != null && byName.isPresent()) {
            List<UserLeaveTypeRemaining> byLeaveTypeAndUser = userLeaveTypeRemainingRepo.findUserLeaveTypeRemainingByEmployeeIDAndLeaveType(employee_id,byName.get());
            if(byLeaveTypeAndUser.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            else
                return byLeaveTypeAndUser.get(0);
        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteUserLeaveTypeRemaining(String publicId) {
        userLeaveTypeRemainingRepo.findByPublicId(publicId).ifPresent(userLeaveTypeRemainingRepo::delete);
    }

    @Override
    public void saveUserLeaveTypeTotal(String employee_id, Integer totalLeaves, String type_name) {
        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);

        if(byName.isPresent() && employee_id != null) {
            LeaveTypeEntity leaveCategoryEntity = byName.get();

            UserLeaveTypeTotalEntity userLeaveTypeTotal = new UserLeaveTypeTotalEntity();
            userLeaveTypeTotal.setPublicId(utils.generateId(10));
            userLeaveTypeTotal.setLeaveType(leaveCategoryEntity);
            userLeaveTypeTotal.setEmployeeID(employee_id);
            userLeaveTypeTotal.setTotalLeaves(totalLeaves);

            userLeaveTypeTotalRepo.save(userLeaveTypeTotal);

        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public UserLeaveTypeTotalEntity getUserLeaveTypeTotal(String employee_id) {
        return null;
    }

    @Override
    public UserLeaveTypeTotalEntity getUserLeaveTypeTotal(String type_name,String employee_id) {
        Optional<LeaveTypeEntity> byName = leaveTypeRepo.findByName(type_name);

        if(employee_id != null && byName.isPresent()) {
            List<UserLeaveTypeTotalEntity> byLeaveTypeAndUser = userLeaveTypeTotalRepo.findUserLeaveTypeTotalByEmployeeIDAndLeaveType(employee_id,byName.get());
            if(byLeaveTypeAndUser.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            else
                return byLeaveTypeAndUser.get(0);
        }else{
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteUserLeaveTypeTotal(String publicId) {
        userLeaveTypeTotalRepo.findByPublicId(publicId).ifPresent(userLeaveTypeTotalRepo::delete);
    }

}
