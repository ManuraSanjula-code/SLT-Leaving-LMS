package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private InOutRepo inOutRepo;

    public Date removeTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);
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

    public Time parseToSqlTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        return Time.valueOf(localTime);
    }

    public void handleLateAndUnsuccessful(String user, AttendanceEntity attendanceEntity) {

        EmployeeEntity employee = getEmployeeById(user);

        if (attendanceEntity == null)
            return;

        attendanceEntity.setIsUnSuccessful(true);

        UserLeaveTypeRemainingEntity remaining_short_Leaves = serviceEvent.getUserLeaveTypeRemaining("Short Leave", employee.getEmployeeId());

        if (remaining_short_Leaves.getRemainingLeaves() < 1) {

            attendanceEntity.setAttendanceType(AttendanceType.HALF_DAY);
            attendanceEntity.setIssueDescription("GOING HALF DAY BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
            attendanceEntity.setDueDateForUA(getDueDate());
        } else {

            attendanceEntity.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
            remaining_short_Leaves.setRemainingLeaves(remaining_short_Leaves.getRemainingLeaves() - 1);
            userLeaveTypeRemainingRepo.save(remaining_short_Leaves);
        }

        Optional<InOutEntity> latest = inOutRepo.findLatestByEmployeeIdAndDate(employee.getSltId(), getYesterdayDate());
        latest.ifPresent(inOutEntity -> attendanceEntity.setLeftTime(inOutEntity.getPunchTypeTime()));

        AttendanceEntity save = attendanceRepo.save(attendanceEntity);
        List<UserLeaveTypeRemainingEntity> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(employee.getEmployeeId());
        boolean nopay =
                userLeaveCategoryRemaining.stream()
                        .allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);

        if (nopay) {
            check_Service.saveNoPayEntity(employee, attendanceEntity,
                    check_Service.createNoPayRequest(save.getAttendanceType().equals(AttendanceType.HALF_DAY),
                            save.getIsUnSuccessful(), save.getIsUnauthorized(),
                            save.getIsLate(), save.getIsLateCovered(),
                            save.getAttendanceType().equals(AttendanceType.ABSENT))
                    , save.getDate());
        }
    }

    public EmployeeEntity getEmployeeById(String employee_id) {
        return employeeRepo.findByPublicId(employee_id)
                .or(() -> employeeRepo.findByEmployeeId(employee_id)
                        .or(() -> employeeRepo.findBySltId(employee_id)))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
    }

    public Optional<EmployeeEntity> getEmployeeByIdV2(String id) {
        return employeeRepo.findByPublicId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByEmployeeId(id));
    }

}