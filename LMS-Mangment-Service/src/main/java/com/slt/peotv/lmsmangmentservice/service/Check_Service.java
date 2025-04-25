package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.model.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;

public interface Check_Service {

    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication);
    public void requestMovement_(MovementReq req, HttpServletRequest request, Authentication authentication);
    @Deprecated
    public void processMovement(String moveId);
    public void processMovement(String moveId, String userId);

    public void main();
    public void prerequisite();
    public void prerequisiteV1();

    public void reportAttendance(InOutEntity inOut,Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,Boolean late, Boolean late_cover,Boolean half_day,Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active,  Boolean nopay, Date date);
    public void reportAttendance(InOutEntity moa, InOutEntity eve,Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,Boolean late, Boolean late_cover,Boolean half_day,Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active,  Boolean nopay, Date date);

    public <T> void reportAttendance(Object obj,Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,Boolean late, Boolean late_cover,Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay, Date date);

    public void reportAbsent(List<String > absentEmployeesToday);
    public void reportAbsent(AbsenteeReq req);

    public void requestALeave(LeaveReq req, String user,Authentication authentication, HttpServletRequest request);
    public void processLeave(String leaveId);
    public void processLeave(String leaveId, String userId);


    public void getAllTheInOutRecordsFromSLT();
    NoPayEntity saveNoPayEntity(String employeeID, AttendanceEntity attendanceEntity, Boolean isHalfDay, Boolean unSuccessful, Boolean isLate, Boolean isLateCover, Boolean isAbsent,
                                Date accualDate);
    Page<InOutDTO> getAllInOut(String employeeID, int pageNumber, int pageSize);
}
