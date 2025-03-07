package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;

import java.util.List;

public interface LMS_Service {

    public List<AbsenteeEntity> getAllAbsentee();
    public AbsenteeEntity getOneAbsentee(String publicId, String employeeId);
    public void saveAbsentee(String employeeId, Boolean isHalfDay, Boolean swipeErr);
    public void deleteAbsentee(String publicId);

    public List<AttendanceEntity> getAllAttendance();
    public List<AttendanceEntity> getAttendanceByUserId(String userId);
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId);

    public void createMovements(MovementsEntity entity);
    public List<MovementsEntity> getAllMovementByUser(String employeeID);
    public List<MovementsEntity> getAllMovements();
    public MovementsEntity getMovement(String publicId);
    public void updateMovement(MovementsEntity entity, String publicId);
    public void deleteMovements(String publicId);

    public void createNoPay(NoPayEntity entity);
    public List<NoPayEntity> getAllNoPayByUser(String employeeID);
    public List<NoPayEntity> getAllNoPays();
    public NoPayEntity getNoPay(String publicId);
    public void deleteNoPay(String publicId);

    public void saveLeave(LeaveEntity entity);
    public List<LeaveEntity> getAllLeaveByUserByPubicId(String employeeID);
    public List<LeaveEntity> getAllLeaveByUserByEmployeeId(String employeeID);
    public List<LeaveEntity> getAllLeaves();
    public LeaveEntity getOneLeave(String publicId);
    public void deleteLeave(String publicId);

    public void saveAttendanceType(String shortName, String Description);
    public AttendanceTypeEntity getAttendanceType(String shortName);
    public void updateAttendanceType(String old_shortName, String shortName, String Description);
    public void deleteAttendanceType(String shortName);

    public void saveLeaveCategory(String name);
    public LeaveCategoryEntity getLeaveCategory(String name);
    public void updateLeaveCategory(String old_name, String name);
    public void deleteLeaveCategory(String name);

    public void saveLeaveType(String name);
    public LeaveTypeEntity getLeaveType(String name);
    public void updateLeaveType(String old_name,String name);
    public void deleteLeaveType(String name);

}
