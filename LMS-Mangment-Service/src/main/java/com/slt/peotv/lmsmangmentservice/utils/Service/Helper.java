package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.repository.AbsenteeRepo;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @Autowired
    private EmployeeRepo employeeRepo;

    public Date removeTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);

        // Reset hour, minute, second and millisecond
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public Date getDueDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.WEEK_OF_YEAR, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public Date getDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public Date getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return removeTimeFromDate(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    public Date getYesterdayDateTest() {
        String mysqlDate = "2024-12-31 00:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime dateTime = LocalDateTime.parse(mysqlDate, formatter);

        // Convert to java.util.Date
        Date legacyDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return legacyDate;
    }

    public void handleAbsenteeReq(String employee, LeaveEntity leaveEntity) {
        List<UserLeaveTypeRemainingEntity> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(leaveEntity.getEmployeeID());
        boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);

        if (employee == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        AbsenteeEntity absenteeEntity = new AbsenteeEntity();
        absenteeEntity.setPublicId(utils.generateId(10));
        absenteeEntity.setEmployeeID(employee);
        absenteeEntity.setDate(new Date());
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);
        absenteeRepo.save(absenteeEntity);


        if (allMatch) { /// NO REMAINING LEAVES

            /// GOING NO PAY -- SET DESCRIPTION IN NO-PAY, FULL DAY IS TURE
            leaveEntity.setIsPending(true);
            leaveEntity.setDescription("EMPLOYEE IS ABSENT ALSO HE/SHE MAKE REQUEST TO LEAVE NOT APPROVED HENCE THIS LEAVE STILL PENDING");

            /// SET FULL DAY IS TURE
            check_Service.saveNoPayEntity(leaveEntity.getEmployeeID(), null,null, false, false, false, false, true, leaveEntity.getHappenDate());

        }
    }

    public void handleAbsenteeReq(String employee, Boolean isHalfDay, Boolean isFullDay) {
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(employee);
        if(employeeEntity.isEmpty()) return;
        AbsenteeEntity absenteeEntity = new AbsenteeEntity();
        absenteeEntity.setPublicId(utils.generateId(10));
        absenteeEntity.setEmployeeID(employee);
        absenteeEntity.setDate(new Date());
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);
        absenteeEntity.setUserId(employeeEntity.get().getPublicId());

        absenteeRepo.save(absenteeEntity);
    }

    public void handleLateAndUnsuccessful(String user, AttendanceEntity attendanceEntity) {

        if (attendanceEntity != null)
            return;

        attendanceEntity.setIsUnSuccessful(true);

        UserLeaveTypeRemainingEntity remaining_short_Leaves = serviceEvent.getUserLeaveTypeRemaining("SHORT_LEAVE", attendanceEntity.getEmployeeID());
        UserLeaveTypeRemainingEntity remaining_half_Day = serviceEvent.getUserLeaveTypeRemaining("HALF_DAY", attendanceEntity.getEmployeeID());

        if (remaining_short_Leaves.getRemainingLeaves() < 1) { /// check are there any short leaves
           /// No short leaves

            attendanceEntity.setIsHalfDay(true);
            attendanceEntity.setIssues(true);

            if (remaining_half_Day.getRemainingLeaves() < 1) { /// check are there any half days
                /// No half days

                attendanceEntity.setIssueDescription("GOING HALF DAY BUT REMAINING HALF DAY IS 0 SO GOING NO-PAY");

                check_Service.saveNoPayEntity(user,null ,attendanceEntity, attendanceEntity.getIsHalfDay(),
                        attendanceEntity.getIsUnSuccessful(), attendanceEntity.getIsLate(),
                        attendanceEntity.getLateCover(), attendanceEntity.getIsAbsent(), attendanceEntity.getDate());

                check_Service.reportAttendance_(attendanceEntity, false, true, false, false, false, true, true, false, false, true, true, attendanceEntity.getDate());

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
                check_Service.reportAttendance_(attendanceEntity, false, true, false, false, false, true, true, false, false, true, false, attendanceEntity.getDate());
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