package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
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

    private Date stripTimeFromDate(Date dateWithTime) {
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

    @Scheduled(cron = "0 00 03 * * ?")
    public void processAllPendingLeaves() {
        List<LeaveEntity> allLeaves = leaveRepository.findAll();
        for (LeaveEntity leave : allLeaves) {

            if(!leave.getIsManualRequest())
                continue;

            if(trackAttendanceDuringLeave(leave, helper.getDateWithoutTime())){
                attendanceProcessingService.processEmployeeLeave(leave.getEmployee(), leave, helper.getDateWithoutTime());
            }

            /*LocalDate startDate = leave.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = leave.getToDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                Date processDate = stripTimeFromDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));

                System.out.println("Processing leave for " + leave.getEmployeeID() + " on " + processDate);
                attendanceProcessingService.processEmployeeLeave(leave.getEmployeeID(), processDate);
            }*/
        }
    }

    public Boolean trackAttendanceDuringLeave(LeaveEntity leaveRequest, Date actualAttendanceDate) {
        // Check if the attendance date falls within the leave period
        return actualAttendanceDate.compareTo(leaveRequest.getFromDate()) >= 0 &&
                actualAttendanceDate.compareTo(leaveRequest.getToDate()) <= 0;
    }
}
