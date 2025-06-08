package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.model.req.*;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.res.DashBoardRes;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

public interface LMS_Service {

    List<InOutDTO> getAllInOuts(String id, boolean swap);
    public Page<AttendanceDTO> getAllAbsent(int page, int size);
    public Page<AttendanceDTO> getAllAbsentByUser(int page, int size, String user);
    public Page<AttendanceDTO> getAllAttendance(int page, int size);
    public Page<AttendanceDTO> getAllAttendanceByUserId(String userId, int page, int size, Boolean admin);
    public Page<AttendanceDTO> getAllAttendanceThatUn(int page, int size);
    public Page<AttendanceDTO> getAllAttendanceThatUnA(int page, int size);
    public Page<AttendanceDTO> getAllAttendanceThatUnByUserId(String userId, int page, int size);
    public Page<AttendanceDTO> getAllAttendanceThatUnAByUserId(String userId, int page, int size);
    public List<AttendanceEntity> getAttendanceByUserId(String userId);
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId);
    public void deleteAttendance(String publicId);
    public void deleteAttendanceV1(String publicId);
    public void makeInAttendanceActive(String publicId);
    public void createMovements(MovementsEntity entity);
    public Page<MovementDTO> getAllMovementByUser(String employeeID, int page, int size,Boolean isAdmin);
    public Page<MovementDTO> getAllMovementByAdmin(String userId, int page, int size, Boolean isAdmin);
    public Page<MovementDTO> getAllMovements(int page, int size, Boolean isAdmin);
    public MovementsEntity getMovement(String publicId);
    public void updateMovement(MovementReq req, String publicId);
    public void deleteMovements(String publicId);

    public void createNoPay(NoPayEntity entity);
    public Page<NopayDTO> getAllNoPayByUser(String employeeID, int page, int size);
    public Page<NopayDTO> getAllNoPays(int page, int size);
    public NoPayEntity getNoPay(String publicId);
    public void deleteNoPay(String publicId);

    public void saveLeave(LeaveEntity entity);
    public Page<LeaveDTO> getAllLeaveByUserByUserId(String userId, int page, int size,Boolean isAdmin);
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(String userId, int page, int size,Boolean isAdmin);
    public Page<LeaveDTO> getAllLeaves(int page, int size,Boolean isAdmin);
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
    public void updateLeaveType(String old_name,String userId, int days);
    public void deleteLeaveType(String name);
    
    LeaveTypeTotDTO getTotalLeaves(String employeeId, String leaveTypeName);
    LeaveTypeRetDTO getRemainingLeaves(String employeeId, String leaveTypeName);
    UserLeaveDetailsDTO getAllLeaveDetails(String employeeId);

    public void updateLeave(LeaveReq req, String leaveId);
    public AttendanceDTO createAttendance(AttendanceReq req);
    public AttendanceDTO updateAttendance(AttendanceReq req, String publicId);
    public DashBoardRes getDashBoard(String userId);

    public void createAccessLog(AccessLogReq req);
    public void createInout(InOutReq req);
    public List<AccessLogEntity> getAccessLog(String employeeId, String date);
    public List<AttendanceDTO> getAttendance(String employeeId, String date);
}
