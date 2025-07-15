package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AttendanceProcessingService {

    @Autowired
    private LeaveRepo leaveRepository;
    @Autowired
    private InOutRepo inOutRepository;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private Check_Service checkService;
    @Autowired
    private Helper helper;

    @Transactional
    public void processEmployeeLeave(EmployeeEntity employee, LeaveEntity leave, Date processDate, boolean swap) {
        System.out.println("Employee Date: " + processDate);

        if(employee == null) return;
        if(swap && !employee.getRoaster()) return;
        
        String employeeId = employee.getEmployeeId();
        if (employeeId.isEmpty())
            return;

        List<InOutEntity> attendanceRecords = inOutRepository.findByEmployeeIdAndDate(employeeId, processDate);

        if (!attendanceRecords.isEmpty()) {
            attendanceRecords.forEach(attendanceRecord -> {
                if (attendanceRecord != null) {
                    InOutEntity inOut = attendanceRecord;

                    boolean isLate = checkLateArrival(inOut);
                    boolean isShortLeave = checkShortLeave(inOut);
                    boolean isHalfDay = checkHalfDay(inOut);
                    boolean isFullDayAttendance = checkFullAttendance(inOut);

                    UserLeaveTypeRemainingEntity remaining_half_Day =
                            serviceEvent.getUserLeaveTypeRemaining("HALF_DAY", employeeId);
                    UserLeaveTypeRemainingEntity remaining_short_Leaves =
                            serviceEvent.getUserLeaveTypeRemaining("SHORT_LEAVE", employeeId);

                    if (isFullDayAttendance) {
                        leave.setNotUsed(true);
                        leave.setRequestStatus(RequestStatus.CANCELLED);
                        leaveRepository.save(leave);
                        leave.setDescription("CAME TO WORK EVEN THOUGH TODAY YOU MAKE A LEAVE BUT YOU CAME AND WORK FULL DAY");
                        checkService.reportAttendance(attendanceRecord,false ,true ,false, false, false, false, false, false, false, true, true, false, false, helper.getYesterdayDate());

                    } else if (isHalfDay) {
                        leave.setNotUsed(true);
                        leave.setRequestStatus(RequestStatus.CANCELLED);
                        leave.setDescription("CAME TO WORK EVEN THOUGH TODAY YOU MAKE A LEAVE BUT YOU CAME TO WORK IN FORM OF A HALF DAY");
                        leaveRepository.save(leave);
                        checkService.reportAttendance(attendanceRecord,false ,false ,false, false, false, false, true, false, false, true, true, false, false, helper.getYesterdayDate());
                    } else if (isShortLeave) {
                        System.out.println();
                    } else if (isLate) {
                        leave.setNotUsed(true);
                        leave.setRequestStatus(RequestStatus.CANCELLED);
                        checkService.reportAttendance(attendanceRecord,false ,false ,false, false, true, false, true, false, false, true, true, false, false, helper.getYesterdayDate());
                        /// IF LATE IS COVER SET LATE_COVER TURE
                    }
                }

                leaveRepository.save(leave);
            });
        } else {
            leave.setNotUsed(false);
            leave.setDescription("Absent - Leave Used");
            LeaveTypeEntity leaveType = leave.getLeaveType();
            leave.setRequestStatus(RequestStatus.SUBMITTED);

            String user = leave.getEmployee().getEmployeeId();
            if (user != null) {
                /*List<UserLeaveTypeRemainingEntity> userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(employeeId); */

                /*switch (leaveType.getName()) {
                    case "CASUAL" -> {
                        UserLeaveTypeRemainingEntity casual = getUserLeaveTypeRemaining("CASUAL", user);
                        if (casual.getRemainingLeaves() > 1) {
                            casual.setRemainingLeaves(casual.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(casual);
                        }
                    }
                    case "ANNUAL" -> {
                        UserLeaveTypeRemainingEntity annual = getUserLeaveTypeRemaining("ANNUAL", user);
                        if (annual.getRemainingLeaves() > 1) {
                            annual.setRemainingLeaves(annual.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(annual);
                        }
                    }
                    case "SICK" -> {
                        UserLeaveTypeRemainingEntity sick = getUserLeaveTypeRemaining("SICK", user);
                        if (sick.getRemainingLeaves() > 1) {
                            sick.setRemainingLeaves(sick.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(sick);
                        }
                    }
                    case "SPECIAL" -> {
                        UserLeaveTypeRemainingEntity special = getUserLeaveTypeRemaining("SPECIAL", user);
                        if (special.getRemainingLeaves() > 1) {
                            special.setRemainingLeaves(special.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(special);
                        }
                    }
                    case "DUTY" -> {
                        UserLeaveTypeRemainingEntity duty = getUserLeaveTypeRemaining("DUTY", user);
                        if (duty.getRemainingLeaves() > 1) {
                            duty.setRemainingLeaves(duty.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(duty);
                        }
                    }
                    case "MATERNITY_LEAVE" -> {
                        UserLeaveTypeRemainingEntity maternityLeave = getUserLeaveTypeRemaining("MATERNITY_LEAVE", user);
                        if (maternityLeave.getRemainingLeaves() > 1) {
                            maternityLeave.setRemainingLeaves(maternityLeave.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(maternityLeave);
                        }
                    }
                    default -> {
                        throw new IllegalArgumentException("Invalid leave type: " + leaveType.getName());
                    }
                }*/

                /*boolean noLeavesRemaining = userLeaveTypeRemaining.stream()
                        .allMatch(leaveType_ -> leaveType_.getRemainingLeaves() < 1);

                if (noLeavesRemaining) {
                    throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
                }*/

                UserLeaveTypeRemainingEntity currentLeave = getUserLeaveTypeRemaining(leaveType.getName(), user);
                if (currentLeave.getRemainingLeaves() > 1) {
                    currentLeave.setRemainingLeaves(currentLeave.getRemainingLeaves() - 1);
                    userLeaveTypeRemainingRepo.save(currentLeave);
                }
                leaveRepository.save(leave);
                checkService.reportAttendance(employeeId,true,false, false, false, false, false, false, true, true, true, true, false, true, helper.getYesterdayDate());

            }
        }
    }

    private UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String name, String user) {
        return serviceEvent.getUserLeaveTypeRemaining(name, user);
    }


    private boolean checkLateArrival(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime().after(Time.valueOf("09:00:00"));
    }

    private boolean checkShortLeave(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime().before(Time.valueOf("16:00:00"));
    }

    private boolean checkHalfDay(InOutEntity inOut) {
        return (inOut.getPunchTime() != null && inOut.getPunchTypeTime() == null);
    }

    private boolean checkFullAttendance(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime() != null;
    }
}
