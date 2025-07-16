package com.slt.peotv.lmsmangmentservice;

import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class Sync {

    private final Check_Service check_Service;
    private final AccessLogService accessLogService;
    private final AttendanceRepo attendanceRepo;

    @Scheduled(cron = "0 0 * * * ?")
    public void getLogs_YES_() throws ParseException {
        check_Service.getAllTheInOutRecordsFromSLT_YES();
        accessLogService.main();
    }

    @Scheduled(cron = "0 0 */2 * * ?")
    public void getLogs_TOD_() throws ParseException {
        check_Service.getAllTheInOutRecordsFromSLT_TOD();
        accessLogService.main();
    }

    @Scheduled(cron = "00 00 02  * * ?")
    public void getLogs() throws ParseException {
        /*LocalDate currentDate = LocalDate.now();

        if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            System.out.println("Today (" + currentDate + ") is Sunday! 🌞");
        } else {
            check_Service.getAllTheInOutRecordsFromSLT();
            accessLogService.main();
            check_Service.main();
        }*/
        /* check_Service.getAllTheInOutRecordsFromSLT();
        accessLogService.main(); */
        check_Service.main();
    }



    @Scheduled(cron = "0 0 6 1 * ?")
    public void makeAsInActive() {
        attendanceRepo.findByIsManualTrue().forEach(attendance -> {
            Date createdDate = attendance.getCreatedDate();
            if (createdDate != null) {
                Calendar oneYearAgo = Calendar.getInstance();
                oneYearAgo.add(Calendar.YEAR, -1);

                if (createdDate.before(oneYearAgo.getTime())) {
                    attendance.setIsManual(false);
                    attendanceRepo.save(attendance);
                }
            }
        });
    }

}
