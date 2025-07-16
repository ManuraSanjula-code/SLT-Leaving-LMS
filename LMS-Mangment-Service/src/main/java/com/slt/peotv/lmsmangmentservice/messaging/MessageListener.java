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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;
import java.util.List;
import com.slt.peotv.lmsmangmentservice.utils.service.AttendanceProcessingService;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;

@Component
public class MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

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

    @Autowired
    private AttendanceProcessingService attendanceProcessingService;

    @Autowired
    private InOutRepo inOutRepo;

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
            if(!employeeEntity.getRoaster()) return;

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
                        leftTime.getMinute(),
                        leftTime.getSecond());
                attendanceEntity.setLeftTime(leftSqlTime);
            }
            if (attendance.getTerminalId() != null) {
                attendanceEntity.setTerminalId(attendance.getTerminalId());
            }

            if (attendance.getAttendanceType() != null) {
                attendanceEntity.setAttendanceType(attendance.getAttendanceType());
            }
            if(attendance.getAttendanceType().equals(AttendanceType.ABSENT)){
                attendanceEntity.setDueDateForUA(helper.getDueDate());
            }
            if (attendance.getLate() != null) {
                attendanceEntity.setIsLate(attendance.getLate());
            }
            if (attendance.getLateCovered() != null) {
                attendanceEntity.setIsLateCovered(attendance.getLateCovered());
            }
            if (attendance.getUnauthorized() != null) {
                attendanceEntity.setIsUnauthorized(attendance.getUnauthorized());
                attendanceEntity.setDueDateForUA(helper.getDueDate());
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
                attendanceEntity.setDueDateForUA(helper.getDueDate());
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

            if(attendance.getAttendanceType().equals(AttendanceType.ABSENT) && attendance.getArrivalDate() != null) {
                List<LeaveEntity> leaveEntities = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual
                        (employeeEntity, attendanceEntity.getArrivalDate(), helper.removeTimeFromDate(new Date()));

                if(leaveEntities != null && !leaveEntities.isEmpty()) {
                    for (LeaveEntity leave : leaveEntities){
                        attendanceProcessingService.processEmployeeLeaveRoaster(leave.getEmployee(), leave, helper.getDateWithoutTime());
                    }
                }
            }

            /* if (attendance.getArrivalDate() != null) {
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
            } */

            if((attendanceEntity.getDate() == null) || (attendanceEntity.getArrivalTime() == null)) {
                return;
            }

            /* if (attendanceRepo.existsByEmployeeAndDate(employeeEntity, attendanceEntity.getDate())) {
                logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                        employeeEntity.getEmployeeId(), helper.getYesterdayDate());
                return;
            } */

            boolean attendanceExists = attendanceRepo.existsByEmployeeAndArrivalDateAndArrivalTime(
                    employeeEntity,
                    attendanceEntity.getDate(),
                    attendanceEntity.getArrivalTime()
            );

            if (attendanceExists) {
                logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                        employeeEntity.getEmployeeId(), attendanceEntity.getDate());
                return;
            }

            attendanceEntity.setIsManual(true);
            AttendanceEntity save = attendanceRepo.save(attendanceEntity);

            updateInOutRelationships(employeeEntity.getSltId(), save);
        }
    }

    private void updateInOutRelationships(String employeeId, AttendanceEntity attendance) {
        try{
            List<InOutEntity> mo = inOutRepo
                    .findByEmployeeIdAndPunchTimeAndPunchTypeTimeAndTerminalId(
                            employeeId,
                            attendance.getArrivalDate(),
                            attendance.getArrivalTime(),
                            attendance.getTerminalId());

            List<InOutEntity> eve = inOutRepo
                    .findByEmployeeIdAndPunchTimeAndPunchTypeTimeAndTerminalId(
                            employeeId,
                            attendance.getArrivalDate(),
                            attendance.getArrivalTime(),
                            attendance.getTerminalId());

            List<InOutEntity> allInOut = new ArrayList<>();
            allInOut.addAll(mo);
            allInOut.addAll(eve);

            for (InOutEntity inOutEntity : allInOut) {
                inOutEntity.setAttendance(attendance);
                inOutRepo.save(inOutEntity);
            }
        }catch (Exception e){
            e.printStackTrace();
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