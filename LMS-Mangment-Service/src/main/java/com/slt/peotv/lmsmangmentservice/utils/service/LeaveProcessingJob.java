package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@Component
public class LeaveProcessingJob {

    @Autowired
    private AttendanceProcessingService attendanceProcessingService;

    @Autowired
    private LeaveRepo leaveRepository;

    @Autowired
    private Helper helper;

    @Scheduled(cron = "0 00 03 * * ?")
    public void processAllPendingLeaves() {
        try {
            List<LeaveEntity> allLeaves = leaveRepository.findAll();
            if (allLeaves == null || allLeaves.isEmpty()) {
                return;
            }

            Date currentDate = helper.getDateWithoutTime();
            if (currentDate == null) {
                System.err.println("Error: Current date is null");
                return;
            }

            for (LeaveEntity leave : allLeaves) {
                try {
                    if (leave == null || !leave.getIsManualRequest() || leave.getEmployee() == null) {
                        continue;
                    }

                    if (isDateWithinLeavePeriod(leave, currentDate)) {
                        attendanceProcessingService.processEmployeeLeave(
                            leave.getEmployee(), 
                            leave, 
                            currentDate
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Error processing leave for employee: " + 
                        (leave != null && leave.getEmployee() != null ? 
                        leave.getEmployee().getEmployeeId() : "unknown") + 
                        " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in leave processing job: " + e.getMessage());
        }
    }

    private boolean isDateWithinLeavePeriod(LeaveEntity leave, Date date) {
        try {
            if (leave == null || date == null || leave.getFromDate() == null || leave.getToDate() == null) {
                return false;
            }
            return !date.before(leave.getFromDate()) && !date.after(leave.getToDate());
        } catch (Exception e) {
            System.err.println("Error checking leave period: " + e.getMessage());
            return false;
        }
    }
}