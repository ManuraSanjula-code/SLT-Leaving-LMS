package com.slt.peotv.lmsmangmentservice.messaging;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.LeaveManagementService;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

@Component
public class MessageListener {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private LeaveRepo leaveRepo;

    @Autowired
    private Helper helper;

    @Autowired
    private LeaveManagementService leaveManagementService;
    
    @JmsListener(destination = "user.queue")
    public void receiveMessage(@Payload LMSUser message) throws JMSException {
        EmployeeEntity employeeEntity = employeeRepo.findByEmail(message.getEmail())
                .orElse(new EmployeeEntity());

        updateEmployeeEntityFromMessage(employeeEntity, message);
        employeeEntity = employeeRepo.save(employeeEntity);
        leaveManagementService.allocateLeaves(employeeEntity);
    }

    @JmsListener(destination = "roster.queue", concurrency = "5-10")
    public void receiveMessage(@Payload Attendance message) throws JMSException {
        updateAttendanceFromMessageNotFull(message);
    }

    private void updateAttendanceFromMessage(Attendance attendance) {
        AttendanceEntity attendanceEntity = new AttendanceEntity();

        attendanceEntity.setPublicId(attendance.getPublicId());
        Optional<EmployeeEntity> employeeEntityOptional = employeeRepo.findByEmployeeId(attendance.getEmployeeId());

        if (employeeEntityOptional.isPresent()) {
            EmployeeEntity employeeEntity = employeeEntityOptional.get();
            attendanceEntity.setEmployee(employeeEntity);
            attendanceEntity.setDate(attendance.getDate());
            attendanceEntity.setArrivalDate(helper.removeTimeFromDate(attendance.getDate()));

            LocalTime arrivalTime = attendance.getArrivalTime();
            Time arriveSqlTime = new Time(arrivalTime.getHour(),
                    arrivalTime.getMinute(),
                    arrivalTime.getSecond());
            attendanceEntity.setArrivalTime(arriveSqlTime);

            LocalTime leftTime = attendance.getLeftTime();
            Time leftSqlTime = new Time(leftTime.getHour(),
                    arrivalTime.getMinute(),
                    arrivalTime.getSecond());

            attendanceEntity.setTerminalId(attendance.getTerminalId());
            attendanceEntity.setAttendanceType(attendance.getAttendanceType());

            attendanceEntity.setArrivalTime(leftSqlTime);
            attendanceEntity.setIsLate(attendance.getLate());
            attendanceEntity.setIsLateCovered(attendance.getLateCovered());
            attendanceEntity.setIsUnauthorized(attendance.getUnauthorized());
            attendanceEntity.setIsUnauthorized(attendance.getUnSuccessful());
            attendanceEntity.setIsHoliday(attendance.getHoliday());
            attendanceEntity.setIsResolved(attendance.getResolved());
            attendanceEntity.setHasIssues(attendance.getHasIssues());
            attendanceEntity.setIsManual(attendance.getManual());
            attendanceEntity.setIssueDescription(attendance.getIssueDescription());
            attendanceEntity.setEtlRunTime(new Date());
            attendanceEntity.setIsManual(true);

            attendanceEntity.setViaLeave(attendance.getViaLeave());
            attendanceEntity.setViaMovement(attendance.getViaMovement());

            Optional<LeaveEntity> leaveEntityOptional = leaveRepo.findByEmployeeAndFromDate(employeeEntity, attendance.getArrivalDate());
            if(leaveEntityOptional.isPresent()){
                LeaveEntity leaveEntity = leaveEntityOptional.get();
                if(leaveEntity.getIsManualRequest() && attendance.getAttendanceType().equals(AttendanceType.ABSENT)){
                    leaveEntity.setRequestStatus(RequestStatus.SUBMITTED);
                    attendanceEntity.setLeaveStatus(LeaveStatus.LEAVE_REQUESTED);
                    attendanceEntity.setLeaveStatus(LeaveStatus.LEAVE_APPROVED);
                }
                if(leaveEntity.getIsManualRequest() && attendance.getAttendanceType().equals(AttendanceType.FULL_DAY)){
                    leaveEntity.setRequestStatus(RequestStatus.EXPIRED);
                    leaveEntity.setNotUsed(true);
                }
                leaveRepo.save(leaveEntity);
            }
            attendanceRepo.save(attendanceEntity);
        }
    }

    private void updateAttendanceFromMessageNotFull(Attendance attendance) {
        if (attendance == null) {
            return;
        }

        AttendanceEntity attendanceEntity = new AttendanceEntity();

        if (attendance.getPublicId() != null) {
            attendanceEntity.setPublicId(attendance.getPublicId());
        }

        if (attendance.getEmployeeId() == null) {
            return;
        }

        Optional<EmployeeEntity> employeeEntityOptional = employeeRepo.findByEmployeeId(attendance.getEmployeeId());

        if (employeeEntityOptional.isPresent()) {
            EmployeeEntity employeeEntity = employeeEntityOptional.get();
            attendanceEntity.setEmployee(employeeEntity);

            if (attendance.getDate() != null) {
                attendanceEntity.setDate(attendance.getDate());
                attendanceEntity.setArrivalDate(helper.removeTimeFromDate(attendance.getDate()));
            }

            if (attendance.getArrivalTime() != null) {
                LocalTime arrivalTime = attendance.getArrivalTime();
                Time arriveSqlTime = new Time(arrivalTime.getHour(),
                        arrivalTime.getMinute(),
                        arrivalTime.getSecond());
                attendanceEntity.setArrivalTime(arriveSqlTime);
            }

            if (attendance.getLeftTime() != null) {
                LocalTime leftTime = attendance.getLeftTime();
                Time leftSqlTime = new Time(leftTime.getHour(),
                        leftTime.getMinute(),  // Fixed: using leftTime instead of arrivalTime
                        leftTime.getSecond());
                attendanceEntity.setLeftTime(leftSqlTime);  // Fixed: was setting arrivalTime instead of leftTime
            }
            if (attendance.getTerminalId() != null) {
                attendanceEntity.setTerminalId(attendance.getTerminalId());
            }

            if (attendance.getAttendanceType() != null) {
                attendanceEntity.setAttendanceType(attendance.getAttendanceType());
            }

            if (attendance.getLate() != null) {
                attendanceEntity.setIsLate(attendance.getLate());
            }
            if (attendance.getLateCovered() != null) {
                attendanceEntity.setIsLateCovered(attendance.getLateCovered());
            }
            if (attendance.getUnauthorized() != null) {
                attendanceEntity.setIsUnauthorized(attendance.getUnauthorized());
            }
            if (attendance.getUnSuccessful() != null) {
                attendanceEntity.setIsUnSuccessful(attendance.getUnSuccessful());
            }
            if (attendance.getHoliday() != null) {
                attendanceEntity.setIsHoliday(attendance.getHoliday());
            }
            if (attendance.getResolved() != null) {
                attendanceEntity.setIsResolved(attendance.getResolved());
            }
            if (attendance.getHasIssues() != null) {
                attendanceEntity.setHasIssues(attendance.getHasIssues());
            }
            if (attendance.getManual() != null) {
                attendanceEntity.setIsManual(attendance.getManual());
            }

            if (attendance.getIssueDescription() != null) {
                attendanceEntity.setIssueDescription(attendance.getIssueDescription());
            }

            attendanceEntity.setEtlRunTime(new Date());

            if (attendance.getViaMovement() != null) {
                attendanceEntity.setViaMovement(attendance.getViaMovement());
            }
            if (attendance.getViaLeave() != null) {
                attendanceEntity.setViaLeave(attendance.getViaLeave());
            }

            if (attendance.getArrivalDate() != null) {
                Optional<LeaveEntity> leaveEntityOptional = leaveRepo.findByEmployeeAndFromDate(employeeEntity, attendance.getArrivalDate());
                if (leaveEntityOptional.isPresent()) {
                    LeaveEntity leaveEntity = leaveEntityOptional.get();
                    if (leaveEntity.getIsManualRequest() && attendance.getAttendanceType() != null && 
                        attendance.getAttendanceType().equals(AttendanceType.ABSENT)) {
                        leaveEntity.setRequestStatus(RequestStatus.SUBMITTED);
                        attendanceEntity.setLeaveStatus(LeaveStatus.LEAVE_REQUESTED);
                        attendanceEntity.setLeaveStatus(LeaveStatus.LEAVE_APPROVED);
                    }
                    if (leaveEntity.getIsManualRequest() && attendance.getAttendanceType() != null && 
                        attendance.getAttendanceType().equals(AttendanceType.FULL_DAY)) {
                        leaveEntity.setRequestStatus(RequestStatus.EXPIRED);
                        leaveEntity.setNotUsed(true);
                    }
                    leaveRepo.save(leaveEntity);
                }
            }

            attendanceRepo.save(attendanceEntity);
        }
    }
    private void updateEmployeeEntityFromMessage(EmployeeEntity employeeEntity, LMSUser message) {
        employeeEntity.setFirstName(message.getFirstName());
        employeeEntity.setLastName(message.getLastName());
        employeeEntity.setEmail(message.getEmail());
        employeeEntity.setEmployeeId(message.getEmployeeId());
        employeeEntity.setSltId(message.getSltId());
        employeeEntity.setJoin_date(message.getJoin_date());
        employeeEntity.setPublicId(message.getPublicId());
        employeeEntity.setRoaster(message.getRoaster());
        employeeEntity.setGender(message.getGender());
    }
}