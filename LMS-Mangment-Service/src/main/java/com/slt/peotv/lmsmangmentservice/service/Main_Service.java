package com.slt.peotv.lmsmangmentservice.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.req.NoPayRequest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Main_Service {

    public void allApproved(BulkApprovedReq bulkApprovedReq, boolean swap);

    public void allReject(BulkApprovedReq bulkApprovedReq, boolean swap);

    public void reject(String id, String userId, boolean swap);

    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication);

    public void processMovement(String moveId, String userId);

    public List<AccessLogRest> getAllAccessLogsToday(String date);

    public List<AccessLogRest> getAllAccessLogs();

    public void main();

    public void prerequisite();

    public Map<String, InOutDTO> getEarliestInOut(String userId, Date date);

    public List<InOutDTO> getEarliestInOutBetweenDate(String userId, Date date, Date date2);

    public List<InOutDTO> getEarliestInOutByDate(String userId, Date date);

    public void reportAttendance(InOutEntity inOut, Boolean swap, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay,Boolean absent, Date date);

    public void reportAttendance(InOutEntity moa, InOutEntity eve, Boolean swap, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay,Boolean absent, Date date);

    public void reportAttendance(String employeeID, Boolean swap, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent,Date date);

    public void reportAbsent(List<String> absentEmployeesToday);

    public void requestALeave(LeaveReq req, String user, Authentication authentication, HttpServletRequest request);

    public void processLeave(String leaveId, String userId);

    public void getAllTheInOutRecordsFromSLT_YES();
    public void getAllTheInOutRecordsFromSLT_TOD();

    public NoPayEntity saveNoPayEntity(EmployeeEntity employee,
                                AttendanceEntity attendanceEntity,
                                NoPayRequest noPayRequest,
                                Date actualDate);

    public Page<InOutDTO> getAllInOut(String employeeID, int pageNumber, int pageSize);

    public List<InOutDTO> getAllInOut(String employeeID, Date date);

    public NoPayRequest createNoPayRequest(Boolean isHalfDay, Boolean unSuccessful, Boolean unAuthorized, Boolean isLate, Boolean isLateCover, Boolean isAbsent);

    public void processUnauthorizedLeave(LeaveEntity leaveEntity, String employeeId);
    public void recalculateAttendanceFromApprovedMovement(AttendanceEntity attendance, MovementsEntity movement);
}
