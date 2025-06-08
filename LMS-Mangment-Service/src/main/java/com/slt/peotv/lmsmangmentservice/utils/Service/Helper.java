package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
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

    public void handleLateAndUnsuccessful(String user, AttendanceEntity attendanceEntity) {

        EmployeeEntity employee = employeeRepo.findByEmployeeId(user)
                .or(() -> employeeRepo.findBySltId(user))
                .or(() -> employeeRepo.findByPublicId(user))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        if (attendanceEntity != null)
            return;

        attendanceEntity.setIsUnSuccessful(true);

        UserLeaveTypeRemainingEntity remaining_short_Leaves = serviceEvent.getUserLeaveTypeRemaining("SHORT_LEAVE", employee.getEmployeeId());
        UserLeaveTypeRemainingEntity remaining_half_Day = serviceEvent.getUserLeaveTypeRemaining("HALF_DAY", employee.getEmployeeId());

        if (remaining_short_Leaves.getRemainingLeaves() < 1) { /// check are there any short leaves
           /// No short leaves

            attendanceEntity.setIsHalfDay(true);
            attendanceEntity.setIssues(true);

            if (remaining_half_Day.getRemainingLeaves() < 1) { /// check are there any half days
                /// No half days

                attendanceEntity.setIssueDescription("GOING HALF DAY BUT REMAINING HALF DAY IS 0 SO GOING NO-PAY");

                check_Service.saveNoPayEntity(employee,null ,attendanceEntity, attendanceEntity.getIsHalfDay(),
                        attendanceEntity.getIsUnSuccessful(), attendanceEntity.getIsLate(),
                        attendanceEntity.getLateCover(), attendanceEntity.getIsAbsent(), attendanceEntity.getDate());


            } else {

                attendanceEntity.setIssueDescription("GOING HALF DAY BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
                attendanceEntity.setDueDateForUA(getDueDate());
                attendanceEntity.setIsHalfDay(true);
                attendanceEntity.setIsAbsent(true);
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