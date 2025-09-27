package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Main_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

@Service
public class Helper {

    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private Main_Service checkService;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private InOutRepo inOutRepo;

    public Date removeTimeFromDate(Date dateWithTime) {
        try {
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
        } catch (Exception e) {
            System.err.println("Error removing time from date: " + e.getMessage());
            return null;
        }
    }

    public Date getDueDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            System.err.println("Error calculating due date: " + e.getMessage());
            return null;
        }
    }

    public Date getDateWithoutTime() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } catch (Exception e) {
            System.err.println("Error getting date without time: " + e.getMessage());
            return null;
        }
    }

    public Date getYesterdayDate() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            return removeTimeFromDate(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } catch (Exception e) {
            System.err.println("Error getting yesterday's date: " + e.getMessage());
            return null;
        }
    }
    
    public void handleLateAndUnsuccessful(String user, AttendanceEntity attendanceEntity, boolean swap) {
        try {
            if (user == null || attendanceEntity == null) {
                System.err.println("Invalid parameters for handleLateAndUnsuccessful");
                return;
            }

            EmployeeEntity employee = getEmployeeById(user);
            if (employee == null) {
                System.err.println("Employee not found for user: " + user);
                return;
            }
            attendanceEntity.setAttendanceType(null);
            if (((attendanceEntity.getLeaveStatus() != null) && attendanceEntity.getLeaveStatus().equals(LeaveStatus.FULL_LEAVE)) || ((attendanceEntity.getAttendanceType() != null) && (attendanceEntity.getAttendanceType().equals(AttendanceType.ABSENT) || attendanceEntity.getAttendanceType().equals(AttendanceType.FULL_DAY)))) {
                return;
            }

            UserLeaveTypeRemainingEntity remainingShortLeaves = serviceEvent.getUserLeaveTypeRemaining("Short Leave", employee.getEmployeeId());

            if (remainingShortLeaves == null || remainingShortLeaves.getRemainingLeaves() < 1) {
                handleNoShortLeaveAvailable(attendanceEntity);
            } else {
                handleShortLeaveAvailable(attendanceEntity, remainingShortLeaves);
            }

            if (!swap) updateAttendanceWithLeaveTime(employee, attendanceEntity);

        } catch (Exception e) {
            System.err.println("Error in handleLateAndUnsuccessful: " + e.getMessage());
        }
    }

    private void handleNoShortLeaveAvailable(AttendanceEntity attendanceEntity) {
        attendanceEntity.setAttendanceType(AttendanceType.HALF_DAY);
        attendanceEntity.setLeaveStatus(null);
        attendanceEntity.setIsUnauthorized(true);
        attendanceEntity.setIssueDescription("GOING HALF DAY AND SET AS UNAUTHORIZED BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        attendanceEntity.setDueDateForUA(getDueDate());
        attendanceRepo.save(attendanceEntity);
    }

    private void handleShortLeaveAvailable(AttendanceEntity attendanceEntity, UserLeaveTypeRemainingEntity remainingShortLeaves) {
        attendanceEntity.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
        remainingShortLeaves.setRemainingLeaves(remainingShortLeaves.getRemainingLeaves() - 1);
        userLeaveTypeRemainingRepo.save(remainingShortLeaves);
        attendanceRepo.save(attendanceEntity);
    }

    private void updateAttendanceWithLeaveTime(EmployeeEntity employee, AttendanceEntity attendanceEntity) {
        try {
            Optional<InOutEntity> latest = inOutRepo.findLatestByEmployeeIdAndDate(employee.getSltId(), attendanceEntity.getArrivalDate() == null ? attendanceEntity.getDate() : attendanceEntity.getArrivalDate());

            if (latest.isPresent()) {
                InOutEntity inOutEntity = latest.get();
                attendanceEntity.setLeftTime(inOutEntity.getPunchTypeTime());
                attendanceRepo.save(attendanceEntity);
            } else {
                attendanceEntity.setIsUnauthorized(true);
                attendanceEntity.setHasIssues(true);
                attendanceEntity.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
                attendanceEntity.setDueDateForUA(getDueDate());
                attendanceRepo.save(attendanceEntity);
            }
        } catch (Exception e) {
            System.err.println("Error updating attendance with leave time: " + e.getMessage());
        }
    }

    public EmployeeEntity getEmployeeById(String employeeId) {
        try {
            if (employeeId == null || employeeId.isEmpty()) {
                return null;
            }

            return Stream.of(
                            employeeRepo.findByPublicId(employeeId),
                            employeeRepo.findByEmployeeId(employeeId),
                            employeeRepo.findBySltId(employeeId)
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
            return null;
        }
    }

    public Optional<EmployeeEntity> getEmployeeByIdV2(String id) {
        try {
            if (id == null || id.isEmpty()) {
                return Optional.empty();
            }

            return Stream.of(
                            employeeRepo.findByPublicId(id),
                            employeeRepo.findBySltId(id),
                            employeeRepo.findByEmployeeId(id)
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } catch (Exception e) {
            System.err.println("Error in getEmployeeByIdV2: " + e.getMessage());
            return Optional.empty();
        }
    }
}