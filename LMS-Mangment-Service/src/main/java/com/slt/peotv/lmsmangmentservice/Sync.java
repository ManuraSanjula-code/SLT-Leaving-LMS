package com.slt.peotv.lmsmangmentservice;

import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Main_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;

@Service
public class Sync {

    @Autowired
    private Main_Service main_Service;
    @Autowired
    private AccessLogService accessLogService;
    @Autowired
    private AttendanceRepo attendanceRepo;

    @Scheduled(cron = "0 0 * * * ?")
    public void getLogs_YES_() throws ParseException {
        main_Service.getAllTheInOutRecordsFromSLT_YES();
        accessLogService.main(false);
    }

    @Scheduled(cron = "0 0 */2 * * ?")
    public void getLogs_TOD_() throws ParseException {
        main_Service.getAllTheInOutRecordsFromSLT_TOD();
        accessLogService.main(true);
    }

    @Scheduled(cron = "00 00 02  * * ?")
    public void getLogs() throws ParseException {
        main_Service.main();
    }

    @Scheduled(cron = "0 0 6 1 * ?")
    public void makeAsInActive() {
        attendanceRepo.findByIsManualTrue().forEach(attendance -> {
            Date createdDate = attendance.getCreatedDate();
            if (createdDate != null) {
                Calendar oneYearAgo = Calendar.getInstance();
                oneYearAgo.add(Calendar.YEAR, -1);

                if (createdDate.before(oneYearAgo.getTime())) {
                    attendance.setManual(false);
                    attendanceRepo.save(attendance);
                }
            }
        });
    }

}
