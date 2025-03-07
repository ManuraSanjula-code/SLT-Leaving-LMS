package com.slt.peotv.lmsmangmentservice.utils.Service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeRemaining;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Date;
import java.util.List;

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

    @Transactional
    public void processEmployeeLeave(String employeeId, Date processDate, Boolean isProved) {
        if (employeeId.isEmpty())
            return;
        List<LeaveEntity> activeLeaves = leaveRepository.findApprovedLeavesByEmployeeIDAndFromDateAndToDate(employeeId, processDate, processDate);

        if (activeLeaves.isEmpty()) {
            return; // No leave to process
        }

        List<InOutEntity> attendanceRecords = inOutRepository.findByEmployeeIDAndDate(employeeId,processDate);

        for (LeaveEntity leave : activeLeaves) {
            attendanceRecords.forEach(attendanceRecord -> {
                if (attendanceRecord != null) {
                    InOutEntity inOut = attendanceRecord;

                    boolean isLate = checkLateArrival(inOut);
                    boolean isShortLeave = checkShortLeave(inOut);
                    boolean isHalfDay = checkHalfDay(inOut);
                    boolean isFullDayAttendance = checkFullAttendance(inOut);

                    UserLeaveTypeRemaining remaining_half_Day =
                            serviceEvent.getUserLeaveTypeRemaining("HALF_DAY", employeeId);
                    UserLeaveTypeRemaining remaining_short_Leaves =
                            serviceEvent.getUserLeaveTypeRemaining("SHORT_LEAVE", employeeId);

                    if (isFullDayAttendance) {
                        leave.setNotUsed(true); // Employee attended fully, so leave is not used.
                        leave.setIsCanceled(true);
                        leaveRepository.save(leave);
                        leave.setDescription("CAME TO WORK EVEN THOUGH TODAY YOU MAKE A LEAVE BUT YOU CAME AND WORK FULL DAY");
                        checkService.reportAttendance(attendanceRecord, true, false, false, false, false, false);

                    } else if (isHalfDay) {
                        leave.setNotUsed(true);
                        leave.setIsCanceled(true);
                        leave.setDescription("CAME TO WORK EVEN THOUGH TODAY YOU MAKE A LEAVE BUT YOU CAME TO WORK IN FORM OF A HALF DAY");
                        leaveRepository.save(leave);
                        checkService.reportAttendance(attendanceRecord, false, true, false, false, false, true);

                    } else if (isShortLeave) {
                        checkService.reportAttendance(attendanceRecord, false, false, true, false, false, false);

                    } else if (isLate) {
                        checkService.reportAttendance(attendanceRecord, false, false, false, true, false, false);
                        /// IF LATE IS COVER SET LATE_COVER TURE
                    }

                } else {
                    leave.setNotUsed(false);
                    // No attendance record found → Employee was absent
                    leave.setDescription("Absent - Leave Used");
                    LeaveTypeEntity leaveType = leave.getLeaveType();

                    String user = leave.getEmployeeID();
                    if (user != null)
                        switch (leaveType.getName()) {
                            case "CASUAL" -> {
                                UserLeaveTypeRemaining casual = getUserLeaveTypeRemaining("CASUAL", user);
                                if (casual.getRemainingLeaves() < 1) {
                                    casual.setRemainingLeaves(casual.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(casual);
                                }
                            }
                            case "ANNUAL" -> {
                                UserLeaveTypeRemaining annual = getUserLeaveTypeRemaining("ANNUAL", user);
                                if (annual.getRemainingLeaves() < 1) {
                                    annual.setRemainingLeaves(annual.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(annual);
                                }
                            }
                            case "SICK" -> {
                                UserLeaveTypeRemaining sick = getUserLeaveTypeRemaining("SICK", user);
                                if (sick.getRemainingLeaves() < 1) {
                                    sick.setRemainingLeaves(sick.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(sick);
                                }
                            }
                            case "SPECIAL" -> {
                                UserLeaveTypeRemaining special = getUserLeaveTypeRemaining("SPECIAL", user);
                                if (special.getRemainingLeaves() < 1) {
                                    special.setRemainingLeaves(special.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(special);
                                }
                            }
                            case "DUTY" -> {
                                UserLeaveTypeRemaining duty = getUserLeaveTypeRemaining("DUTY", user);
                                if (duty.getRemainingLeaves() < 1) {
                                    duty.setRemainingLeaves(duty.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(duty);
                                }
                            }
                            case "MATERNITY_LEAVE" -> {
                                UserLeaveTypeRemaining maternityLeave = getUserLeaveTypeRemaining("MATERNITY_LEAVE", user);
                                if (maternityLeave.getRemainingLeaves() < 1) {
                                    maternityLeave.setRemainingLeaves(maternityLeave.getRemainingLeaves() - 1);
                                    userLeaveTypeRemainingRepo.save(maternityLeave);
                                }
                            }
                            default -> {
                                throw new IllegalArgumentException("Invalid leave type: " + leaveType.getName());
                            }
                        }
                    else
                        return;
                }

                leaveRepository.save(leave);
            });
        }
    }

    private UserLeaveTypeRemaining getUserLeaveTypeRemaining(String name, String user) {
        return serviceEvent.getUserLeaveTypeRemaining(name, user);
    }


    private boolean checkLateArrival(InOutEntity inOut) {
        return inOut.getPunchInMoa() != null && inOut.getTimeMoa().after(Time.valueOf("09:00:00"));
    }

    private boolean checkShortLeave(InOutEntity inOut) {
        return inOut.getTimeEve() != null && inOut.getTimeEve().before(Time.valueOf("16:00:00"));
    }

    private boolean checkHalfDay(InOutEntity inOut) {
        return (inOut.getTimeMoa() != null && inOut.getTimeEve() == null) ||
                (inOut.getTimeMoa() == null && inOut.getTimeEve() != null);
    }

    private boolean checkFullAttendance(InOutEntity inOut) {
        return inOut.getTimeMoa() != null && inOut.getTimeEve() != null;
    }
}

