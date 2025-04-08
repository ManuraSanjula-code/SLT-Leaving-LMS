package com.slt.peotv.lmsmangmentservice;

import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class CommandLine implements CommandLineRunner {

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private Check_Service checkService;

    @Override
    public void run(String... args) throws Exception {
//        accessLogService.main();
//        checkService.prerequisite_test();
//        checkService.main();
    }
}
