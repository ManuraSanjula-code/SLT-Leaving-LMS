package com.slt.peotv.lmsmangmentservice.messaging;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.slt.peotv.lmsmangmentservice.utils.service.AttendanceProcessingService;

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
        try {
            Optional<EmployeeEntity> employeeOptional = Stream.of(
                            employeeRepo.findBySltId(message.getSltId()),
                            employeeRepo.findByEmployeeId(message.getEmployeeId()),
                            employeeRepo.findByEmail(message.getEmail())
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            if (employeeOptional.isPresent()) {
                EmployeeEntity employeeEntity = employeeOptional.get();
                updateEmployeeEntityFromMessage(employeeEntity, message);
                employeeRepo.save(employeeEntity);
                leaveManagementService.allocateLeaves(employeeEntity);
            } else {
                if (!employeeRepo.existsByEmployeeId(message.getEmployeeId())) {
                    EmployeeEntity newEmployee = new EmployeeEntity();
                    updateEmployeeEntityFromMessage(newEmployee, message);
                    employeeRepo.save(newEmployee);
                    leaveManagementService.allocateLeaves(newEmployee);
                } else {
                    logger.warn("Employee with ID {} already exists", message.getEmployeeId());
                }
            }
        } catch (Exception e) {
            logger.error("Error processing employee message for employeeId: {}", message.getEmployeeId(), e);
            throw new JMSException("Error processing employee message: " + e.getMessage());
        }
    }

    @JmsListener(destination = "roster.queue", concurrency = "5-10")
    public void receiveMessage(@Payload AttendanceJSM message) throws JMSException {
        try {
            updateAttendanceFromMessageNotFull(message);
        } catch (Exception e) {
            logger.error("Error processing attendance message", e);
            throw new JMSException("Error processing attendance message: " + e.getMessage());
        }
    }

    private void updateAttendanceFromMessageNotFull(AttendanceJSM attendance) {
        if (attendance == null) {
            return;
        }

        try {
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
                    attendanceEntity.setDate(helper.removeTimeFromDate(attendance.getDate()));
                }else{
                    attendanceEntity.setDate(helper.removeTimeFromDate(helper.getYesterdayDate()));
                }

                if(attendance.getArrivalDate() != null){
                    attendanceEntity.setArrivalDate(helper.removeTimeFromDate(attendance.getArrivalDate()));
                }else{
                    attendanceEntity.setArrivalDate(helper.removeTimeFromDate(helper.getYesterdayDate()));
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

                attendanceEntity.setIsLate(attendance.getLate());
                attendanceEntity.setIsLateCovered(attendance.getLateCovered());
                attendanceEntity.setIsResolved(attendance.getResolved());
                attendanceEntity.setHasIssues(attendance.getHasIssues());
                attendanceEntity.setIsHoliday(attendance.getHoliday());
                attendanceEntity.setIsUnSuccessful(attendance.getUnSuccessful());
                attendanceEntity.setIsUnauthorized(attendance.getUnauthorized());

                if(attendance.getLate() || attendance.getLateCovered() || attendance.getHasIssues() ||
                        attendance.getUnSuccessful() || attendance.getUnauthorized() ||
                        (attendance.getAttendanceType() != null && attendance.getAttendanceType().equals(AttendanceType.ABSENT))) {
                    attendanceEntity.setDueDateForUA(helper.getDueDate());
                }

                if (attendance.getIssueDescription() != null) {
                    attendanceEntity.setIssueDescription(attendance.getIssueDescription());
                }

                if (attendance.getRosterType() != null) {
                    attendanceEntity.setRosterType(attendance.getRosterType());
                }

                if (attendance.getLeaveStatus() != null) {
                    attendanceEntity.setLeaveStatus(attendance.getLeaveStatus());
                }

                if (attendance.getViaMovement() != null) {
                    attendanceEntity.setViaMovement(attendance.getViaMovement());
                }

                if (attendance.getViaLeave() != null) {
                    attendanceEntity.setViaLeave(attendance.getViaLeave());
                }

                if(attendance.getAttendanceType() != null &&
                        attendance.getAttendanceType().equals(AttendanceType.ABSENT) &&
                        attendance.getArrivalDate() != null) {
                    List<LeaveEntity> leaveEntities = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual
                            (employeeEntity, attendanceEntity.getArrivalDate(), attendanceEntity.getArrivalDate());

                    if(leaveEntities != null && !leaveEntities.isEmpty()) {
                        for (LeaveEntity leave : leaveEntities){
                            attendanceProcessingService.processEmployeeLeave(leave.getEmployee(), leave, helper.getDateWithoutTime());
                        }
                    }
                }

                if (attendanceRepo.existsByEmployeeAndDate(employeeEntity, attendanceEntity.getDate())) {
                    logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                            employeeEntity.getEmployeeId(), helper.getYesterdayDate());
                    return;
                }

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

                attendanceEntity.setEtlRunTime(new Date());
                attendanceEntity.setIsManual(true);
                AttendanceEntity save = attendanceRepo.save(attendanceEntity);

                updateInOutRelationships(employeeEntity.getSltId(), save);
            }
        } catch (Exception e) {
            logger.error("Error updating attendance for employeeId: {}", attendance.getEmployeeId(), e);
        }
    }

    private void updateInOutRelationships(String employeeId, AttendanceEntity attendance) {
        try {
            if(attendance == null || employeeId == null) return;

            Date arrivalDate = attendance.getArrivalDate();

            Optional<InOutEntity> eve = inOutRepo.findLatestByEmployeeIdAndDate(
                    employeeId,
                    arrivalDate);

            Optional<InOutEntity> mo = inOutRepo.findEarliestByEmployeeIdAndDate(
                    employeeId,
                    arrivalDate);

            if(mo.isPresent()){
                InOutEntity inOutEntity = mo.get();
                inOutEntity.setAttendance(attendance);
                inOutRepo.save(inOutEntity);
            }

            if(eve.isPresent()){
                InOutEntity inOutEntity = eve.get();
                inOutEntity.setAttendance(attendance);
                inOutRepo.save(inOutEntity);
            }

        } catch (Exception e) {
            logger.error("Error updating in-out relationships for employeeId: {}", employeeId, e);
        }
    }

    private void updateEmployeeEntityFromMessage(EmployeeEntity employeeEntity, LMSUser message) {
        if (message.getFirstName() != null) {
            employeeEntity.setFirstName(message.getFirstName());
        }
        if (message.getLastName() != null) {
            employeeEntity.setLastName(message.getLastName());
        }
        if (message.getEmail() != null) {
            employeeEntity.setEmail(message.getEmail());
        }
        if (message.getEmployeeId() != null) {
            employeeEntity.setEmployeeId(message.getEmployeeId());
        }
        if (message.getSltId() != null) {
            employeeEntity.setSltId(message.getSltId());
        }
        if (message.getJoin_date() != null) {
            employeeEntity.setJoin_date(message.getJoin_date());
        }
        if (message.getPublicId() != null) {
            employeeEntity.setPublicId(message.getPublicId());
        }
        if (message.getRoaster() != null) {
            employeeEntity.setRoaster(message.getRoaster());
        }
        if (message.getGender() != null) {
            employeeEntity.setGender(message.getGender());
        }
    }
}