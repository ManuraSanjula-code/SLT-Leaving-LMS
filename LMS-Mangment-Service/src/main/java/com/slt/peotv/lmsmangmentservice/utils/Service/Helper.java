package com.slt.peotv.lmsmangmentservice.utils.Service;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeRemaining;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.repository.AbsenteeRepo;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class Helper {

    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private Check_Service check_Service;
    @Autowired
    private LMS_Service lmsService;
    @Autowired
    private Utils utils;
    @Autowired
    private AbsenteeRepo absenteeRepo;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;

    public Date getDueDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);  // Add 1 month
        calendar.add(Calendar.WEEK_OF_YEAR, 1); // Add 1 extra week
        return calendar.getTime(); // Return as Date object
    }

    public Date getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    public void handleAbsenteeReqFull(String employee, LeaveEntity leaveEntity) {
        List<UserLeaveTypeRemaining> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(leaveEntity.getEmployeeID());
        boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);


        if (employee == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(employee);
        attendance.setDate(new Date());

        attendance.setIsLate(false);
        attendance.setLateCover(false);
        attendance.setIsUnSuccessful(false);

        attendance.setIsUnAuthorized(true);
        attendance.setIsAbsent(true);

        attendance.setIsFullDay(false);
        attendance.setIsHalfDay(false);
        attendance.setDueDateForUA(getDueDate());
        attendance.setIssues(true);

        attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  " + "ABSENT WITH OUT MAKING A LEAVE " +
                " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        attendanceRepo.save(attendance);

        /// **************************************************************

        AbsenteeEntity absenteeEntity = new AbsenteeEntity();
        absenteeEntity.setPublicId(utils.generateId(10));
        absenteeEntity.setEmployeeID(employee);
        absenteeEntity.setDate(new Date());
        absenteeEntity.setIsHODApproved(false);
        absenteeEntity.setIsSupervisedApproved(false);
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);

        absenteeEntity.setIsAbsent(true);
        absenteeEntity.setIsLate(false);
        absenteeEntity.setIsLateCover(false);
        absenteeEntity.setIsUnSuccessfulAttdate(false);
        absenteeEntity.setIsHalfDay(false);
        absenteeEntity.setComment("EMPLOYEE ABSENT IN TODAY");
        absenteeEntity.setHappenDate(getYesterdayDate());

        absenteeEntity.setIsPending(false);
        absenteeEntity.setIsAccepted(false);

        absenteeRepo.save(absenteeEntity);


        if (allMatch) { /// NO REMAINING LEAVES

            /// GOING NO PAY -- SET DESCRIPTION IN NO-PAY, FULL DAY IS TURE
            leaveEntity.setIsPending(true);
            leaveEntity.setDescription("EMPLOYEE IS ABSENT ALSO HE/SHE MAKE REQUEST TO LEAVE NOT APPROVED HENCE THIS LEAVE STILL PENDING");

            /// SET FULL DAY IS TURE
            check_Service.saveNoPayEntity(leaveEntity.getEmployeeID(), null, false, false, false, false, true, leaveEntity.getHappenDate());

        }

        check_Service.reportAttendance(employee, false, true, false, false, false, false);
    }

    public void handleAbsenteeReqFull(String employee) {
        List<UserLeaveTypeRemaining> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(employee);
        boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);

        if (employee == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(employee);
        attendance.setDate(new Date());

        attendance.setIsLate(false);
        attendance.setLateCover(false);
        attendance.setIsUnSuccessful(false);

        attendance.setIsUnAuthorized(true);
        attendance.setIsAbsent(true);

        attendance.setIsFullDay(false);
        attendance.setIsHalfDay(false);
        attendance.setDueDateForUA(getDueDate());
        attendance.setIssues(true);

        attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  " + "ABSENT WITH OUT MAKING A LEAVE " +
                " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        attendanceRepo.save(attendance);

        /// **************************************************************

        AbsenteeEntity absenteeEntity = new AbsenteeEntity();
        absenteeEntity.setPublicId(utils.generateId(10));
        absenteeEntity.setEmployeeID(employee);
        absenteeEntity.setDate(new Date());
        absenteeEntity.setIsHODApproved(false);
        absenteeEntity.setIsSupervisedApproved(false);
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);

        absenteeEntity.setIsAbsent(true);
        absenteeEntity.setIsLate(false);
        absenteeEntity.setIsLateCover(false);
        absenteeEntity.setIsUnSuccessfulAttdate(false);
        absenteeEntity.setIsHalfDay(false);
        absenteeEntity.setComment("EMPLOYEE ABSENT IN TODAY");
        absenteeEntity.setHappenDate(getYesterdayDate());

        absenteeEntity.setIsPending(false);
        absenteeEntity.setIsAccepted(false);

        absenteeRepo.save(absenteeEntity);

        check_Service.reportAttendance(employee, false, true, false, false, false, false);
    }

    public void handleAbsenteeReqHalf(String employee) {
        AbsenteeReq req = new AbsenteeReq();
        req.setEmployeeId(employee);
        req.setIsHalfDay(true);
        req.setHappenDate(getYesterdayDate());
        req.setComment("GOING HALF DAY WITH-OUT NOTIFYING");

        check_Service.reportAbsent(req);

        check_Service.reportAttendance(employee, false, true, false, false, false, true);
    }

    public void handleLateAndUnsuccessful(String user, AttendanceEntity attendanceEntity) {

        if (attendanceEntity != null)
            return;

        attendanceEntity.setIsUnSuccessful(true);

        UserLeaveTypeRemaining remaining_short_Leaves = serviceEvent.getUserLeaveTypeRemaining("SHORT_LEAVE", attendanceEntity.getEmployeeID());
        UserLeaveTypeRemaining remaining_half_Day = serviceEvent.getUserLeaveTypeRemaining("HALF_DAY", attendanceEntity.getEmployeeID());

        if (remaining_short_Leaves.getRemainingLeaves() < 1) { /// check are there any short leaves
           /// No short leaves

            attendanceEntity.setIsHalfDay(true);
            attendanceEntity.setIssues(true);

            if (remaining_half_Day.getRemainingLeaves() < 1) { /// check are there any half days
                /// No half days

                attendanceEntity.setIssueDescription("GOING HALF DAY BUT REMAINING HALF DAY IS 0 SO GOING NO-PAY");

                check_Service.saveNoPayEntity(user, attendanceEntity, attendanceEntity.getIsHalfDay(),
                        attendanceEntity.getIsUnSuccessful(), attendanceEntity.getIsLate(),
                        attendanceEntity.getLateCover(), attendanceEntity.getIsAbsent(), attendanceEntity.getDate());
            } else {

                attendanceEntity.setIssueDescription("GOING HALF DAY BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
                attendanceEntity.setDueDateForUA(getDueDate());

                /// there are half days
                /// there are half days consider as UnSuccessful Leave ======================

                AbsenteeReq req = new AbsenteeReq();
                req.setEmployeeId(user);
                req.setIsHalfDay(true);
                req.setHappenDate(attendanceEntity.getDate());
                req.setComment("GOING HALF DAY WITH-OUT NOTIFYING");

                check_Service.reportAbsent(req);
                check_Service.reportAttendance(attendanceEntity, false, true, false, false, false, true);
            }

        } else {
            /// there are short leaves

            attendanceEntity.setIsShortLeave(true);
            attendanceEntity.setIssues(true);

            remaining_short_Leaves.setRemainingLeaves(remaining_short_Leaves.getRemainingLeaves() - 1);
            userLeaveTypeRemainingRepo.save(remaining_short_Leaves);
        }

        assert attendanceEntity != null;
        attendanceRepo.save(attendanceEntity);
    }
}