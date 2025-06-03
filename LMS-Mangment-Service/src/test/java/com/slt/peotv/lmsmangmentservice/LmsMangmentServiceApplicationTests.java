package com.slt.peotv.lmsmangmentservice;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.repository.AccessLogRepo;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
public class LmsMangmentServiceApplicationTests {

    @Autowired
    private AccessLogRepo accessLogRepo;

    @Autowired
    private Helper helper;

    private static final String[] EMPLOYEE_IDS = {"A00012", "A00015", "A00020", "A00025", "A00029", "A00036"};
    private static final String[] TERMINALS = {"SLT-HQ01", "SLT-HQ02", "SLT-HQ03"};
    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    public void generateRandomAccessLogs() {

        List<AccessLogEntity> logs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        String todayDate = dateFormat.format(helper.getYesterdayDate());

        // For each employee
        for (String empId : EMPLOYEE_IDS) {
            // 80% chance of having morning entry
            if (random.nextDouble() < 0.8) {
                logs.add(createLog(empId, todayDate,
                        generateMorningTime(),
                        randomTerminal(),
                        "IN        "));
            }

            // 70% chance of having evening entry
            if (random.nextDouble() < 0.7) {
                logs.add(createLog(empId, todayDate,
                        generateEveningTime(),
                        randomTerminal(),
                        "OUT       "));
            }

            // 20% chance of having additional entries (lunch breaks, etc)
            if (random.nextDouble() < 0.2) {
                logs.add(createLog(empId, todayDate,
                        generateLunchOutTime(),
                        randomTerminal(),
                        "OUT       "));

                logs.add(createLog(empId, todayDate,
                        generateLunchInTime(),
                        randomTerminal(),
                        "IN        "));
            }
        }

        // Save all logs
        accessLogRepo.saveAll(logs);
    }

    private AccessLogEntity createLog(String empId, String date, String time,
                                      String terminal, String inOut) {
        String paddedTerminal = String.format("%-20s", terminal);

        return AccessLogEntity.builder()
                .employeeID(empId)
                .logDate(date)
                .logTime(String.format("%-10s", time)) // Pad time to 10 chars
                .terminalID(paddedTerminal)
                .inOut(inOut)
                .readStatus("0")
                .processed(1)
                .etlRunTime(generateEtlRunTime(date))
                .build();
    }

    private String generateMorningTime() {
        // Between 7:45 and 9:30
        int hour = 7 + random.nextInt(2); // 7 or 8
        int minute = random.nextInt(60);
        if (hour == 8 && minute > 30) minute = 30; // Cap at 8:30 for most employees
        return String.format("%02d:%02d:%02d", hour, minute, random.nextInt(60));
    }

    private String generateEveningTime() {
        // Between 16:30 and 19:00
        int hour = 16 + random.nextInt(3); // 16, 17, or 18
        int minute = random.nextInt(60);
        if (hour == 18 && minute > 0) minute = 0; // After 18:00 becomes 18:00
        return String.format("%02d:%02d:%02d", hour, minute, random.nextInt(60));
    }

    private String generateLunchOutTime() {
        // Between 12:00 and 13:00
        return String.format("%02d:%02d:%02d", 12, random.nextInt(60), random.nextInt(60));
    }

    private String generateLunchInTime() {
        // Between 13:00 and 14:00
        return String.format("%02d:%02d:%02d", 13, random.nextInt(60), random.nextInt(60));
    }

    private String randomTerminal() {
        return TERMINALS[random.nextInt(TERMINALS.length)];
    }

    private Date generateEtlRunTime(String logDate) {
        try {
            // Parse the log date and add random hours (1-24) and minutes
            Date date = dateFormat.parse(logDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, 1 + random.nextInt(24));
            cal.add(Calendar.MINUTE, random.nextInt(60));
            return cal.getTime();
        } catch (Exception e) {
            return new Date();
        }
    }
}