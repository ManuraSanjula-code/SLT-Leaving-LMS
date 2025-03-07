package com.slt.peotv.lmsmangmentservice.utils.Service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class LeaveProcessingJob {

    @Autowired
    private AttendanceProcessingService attendanceProcessingService;

    @Autowired
    private LeaveRepo leaveRepository;

    @Scheduled(cron = "0 0 1 * * ?") // Runs every day at 1 AM
    public void processAllPendingLeaves() {
        List<LeaveEntity> allLeaves = leaveRepository.findAll(); // Fetch all leave requests

        for (LeaveEntity leave : allLeaves) {
            LocalDate startDate = leave.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = leave.getToDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                Date processDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                if(leave.getIsSupervisedApproved() && leave.getIsHODApproved())
                    attendanceProcessingService.processEmployeeLeave(leave.getEmployeeID(), processDate, true);
                else
                    attendanceProcessingService.processEmployeeLeave(leave.getEmployeeID(), processDate, false);
            }
        }
    }
}