package com.slt.peotv.lmsmangmentservice;


import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class Sync {

    private final Check_Service check_Service;
    private final AccessLogService accessLogService;

    @Scheduled(cron = "00 37 08  * * ?")
    public void getLogs() throws ParseException {
        check_Service.getAllTheInOutRecordsFromSLT();
        accessLogService.main();
        check_Service.main();
    }
}
