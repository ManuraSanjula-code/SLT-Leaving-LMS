package com.slt.peotv.lmsmangmentservice;


import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class Sync {

    private final Check_Service check_Service;
    private final AccessLogService accessLogService;

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
        check_Service.getAllTheInOutRecordsFromSLT();
        accessLogService.main();
        check_Service.main();
    }
        
}
