package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Check_Service {

    public void allApproved(BulkApprovedReq bulkApprovedReq, boolean swap);
    public void allReject(BulkApprovedReq bulkApprovedReq, boolean swap);
    public void reject(String id, String userId, boolean swap);

    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication);
    public void processMovement(String moveId, String userId);
    public List<AccessLogRest> getAllAccessLogsToday(String date);
    public List<AccessLogRest> getAllAccessLogs();
    public void main();
    public void prerequisite();

    Map<String, InOutDTO> getEarliestInOut(String userId, Date date);
    List<InOutDTO> getEarliestInOutBetweenDate(String userId, Date date, Date date2);
    List<InOutDTO> getEarliestInOutByDate(String userId, Date date);

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
    List<InOutDTO> getAllInOut(String employeeID, Date date);

}
