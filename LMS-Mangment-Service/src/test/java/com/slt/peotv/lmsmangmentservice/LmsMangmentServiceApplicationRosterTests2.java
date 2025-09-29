/*
package com.slt.peotv.lmsmangmentservice;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.repository.AccessLogRepo;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
@SpringBootTest
public class LmsMangmentServiceApplicationRosterTests2 {

    @Autowired
    private AccessLogRepo accessLogRepo;

    @Autowired
    private Helper helper;

    private static final String[] TERMINALS = {"SLT-HQ01", "SLT-HQ02", "SLT-HQ03"};
    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // NEW: Specific employee test cases with exact time shifts
    @Test
    public void generateSpecificEmployeeShiftTestCases() {

        List<AccessLogEntity> logs = new ArrayList<>();
        String todayDate = dateFormat.format(helper.getYesterdayDate());

        // Target specific employees
        String[] TARGET_EMPLOYEES = {"A00062", "A00065", "A00048"};

        // Define exact shift schedules
        Map<String, ShiftSchedule> employeeShifts = new HashMap<>();
        employeeShifts.put("A00062", new ShiftSchedule(0, 8));   // Night: 00:00 - 08:00
        employeeShifts.put("A00065", new ShiftSchedule(8, 16));  // Day: 08:00 - 16:00
        employeeShifts.put("A00048", new ShiftSchedule(16, 24)); // Evening: 16:00 - 24:00

        // Generate logs for each specific employee
        for (String empId : TARGET_EMPLOYEES) {
            ShiftSchedule shift = employeeShifts.get(empId);
            generateExactShiftLogs(logs, empId, todayDate, shift);
        }

        // Save all logs
        accessLogRepo.saveAll(logs);

        System.out.println("Generated " + logs.size() + " test case logs for employees: " +
                String.join(", ", TARGET_EMPLOYEES) + " for date: " + todayDate);

        // Print summary for verification
        printTestCaseSummary(logs, employeeShifts);
    }

    private void generateExactShiftLogs(List<AccessLogEntity> logs, String empId, String todayDate, ShiftSchedule shift) {

        // Case 1: Employee arrives BEFORE shift start (like 23:23 PM for 00:00-08:00 shift)
        if (shift.startHour == 0) { // Night shift
            // Arrive previous day around 23:00-23:59
            String earlyArrival = generateTimeBeforeShift(23, 0, 59);
            logs.add(createLog(empId, todayDate, earlyArrival, randomTerminal(), "IN        "));
            System.out.println("Employee " + empId + " early arrival: " + earlyArrival + " (before " +
                    String.format("%02d:00", shift.startHour) + " shift)");
        } else {
            // For day/evening shifts, arrive 30-60 minutes early
            int earlyHour = shift.startHour - 1;
            String earlyArrival = generateTimeBeforeShift(earlyHour, 0, 59);
            logs.add(createLog(empId, todayDate, earlyArrival, randomTerminal(), "IN        "));
            System.out.println("Employee " + empId + " early arrival: " + earlyArrival + " (before " +
                    String.format("%02d:00", shift.startHour) + " shift)");
        }

        // Case 2: Official shift start entry
        String shiftStartTime = String.format("%02d:%02d:%02d", shift.startHour,
                random.nextInt(15), random.nextInt(60)); // Within first 15 minutes
        logs.add(createLog(empId, todayDate, shiftStartTime, randomTerminal(), "IN        "));

        // Case 3: Mid-shift break (OUT then IN)
        int midShiftHour = shift.startHour + (shift.endHour - shift.startHour) / 2;
        if (midShiftHour >= 24) midShiftHour = midShiftHour % 24;

        String breakOutTime = String.format("%02d:%02d:%02d", midShiftHour,
                random.nextInt(60), random.nextInt(60));
        logs.add(createLog(empId, todayDate, breakOutTime, randomTerminal(), "OUT       "));

        // Return from break (30-60 minutes later)
        int returnMinute = (Integer.parseInt(breakOutTime.split(":")[1]) + 30 + random.nextInt(30)) % 60;
        int returnHour = midShiftHour;
        if (Integer.parseInt(breakOutTime.split(":")[1]) + 30 + random.nextInt(30) >= 60) {
            returnHour = (returnHour + 1) % 24;
        }

        String breakInTime = String.format("%02d:%02d:%02d", returnHour, returnMinute, random.nextInt(60));
        logs.add(createLog(empId, todayDate, breakInTime, randomTerminal(), "IN        "));

        // Case 4: Shift end (exactly at scheduled time or slightly after)
        int endHour = shift.endHour == 24 ? 23 : shift.endHour;
        int endMinute = shift.endHour == 24 ? 45 + random.nextInt(15) : random.nextInt(30); // End within 30 min of scheduled

        String shiftEndTime = String.format("%02d:%02d:%02d", endHour, endMinute, random.nextInt(60));
        logs.add(createLog(empId, todayDate, shiftEndTime, randomTerminal(), "OUT       "));

        // Case 5: Late departure (some employees stay longer)
        if (random.nextDouble() < 0.3) { // 30% chance of overtime
            int overtimeHour = (shift.endHour + 1) % 24;
            String overtimeOut = String.format("%02d:%02d:%02d", overtimeHour,
                    random.nextInt(60), random.nextInt(60));
            logs.add(createLog(empId, todayDate, overtimeOut, randomTerminal(), "OUT       "));
        }
    }

    private String generateTimeBeforeShift(int baseHour, int minMinute, int maxMinute) {
        int minute = minMinute + random.nextInt(maxMinute - minMinute + 1);
        int second = random.nextInt(60);
        return String.format("%02d:%02d:%02d", baseHour, minute, second);
    }

    private void printTestCaseSummary(List<AccessLogEntity> logs, Map<String, ShiftSchedule> employeeShifts) {
        System.out.println("\n=== TEST CASE SUMMARY ===");

        Map<String, List<AccessLogEntity>> logsByEmployee = logs.stream()
                .collect(Collectors.groupingBy(AccessLogEntity::getEmployeeId));

        for (Map.Entry<String, List<AccessLogEntity>> entry : logsByEmployee.entrySet()) {
            String empId = entry.getKey();
            List<AccessLogEntity> empLogs = entry.getValue();
            ShiftSchedule shift = employeeShifts.get(empId);

            System.out.println("\nEmployee: " + empId + " | Shift: " +
                    String.format("%02d:00 - %02d:00", shift.startHour, shift.endHour));

            empLogs.sort((a, b) -> a.getLogTime().trim().compareTo(b.getLogTime().trim()));

            for (AccessLogEntity log : empLogs) {
                String time = log.getLogTime().trim();
                String action = log.getInOut().trim();
                String terminal = log.getTerminalId().trim();

                String annotation = "";
                int logHour = Integer.parseInt(time.split(":")[0]);

                if (action.equals("IN") && logHour < shift.startHour && shift.startHour != 0) {
                    annotation = " <- EARLY ARRIVAL";
                } else if (action.equals("IN") && shift.startHour == 0 && logHour >= 23) {
                    annotation = " <- EARLY ARRIVAL (NIGHT SHIFT)";
                } else if (action.equals("IN") && logHour == shift.startHour) {
                    annotation = " <- SHIFT START";
                } else if (action.equals("OUT") && logHour >= shift.endHour) {
                    annotation = " <- SHIFT END";
                } else if (action.equals("OUT") && logHour > shift.startHour && logHour < shift.endHour) {
                    annotation = " <- BREAK OUT";
                } else if (action.equals("IN") && logHour > shift.startHour && logHour < shift.endHour) {
                    annotation = " <- BREAK IN";
                }

                System.out.println("  " + time + " | " + action + " | " + terminal + annotation);
            }
        }
    }

    // Helper class for shift scheduling
    private static class ShiftSchedule {
        final int startHour;
        final int endHour;

        ShiftSchedule(int start, int end) {
            this.startHour = start;
            this.endHour = end;
        }
    }

    // Alternative test method for even more specific scenarios
    @Test
    public void generateExtremeEdgeCaseTestData() {

        List<AccessLogEntity> logs = new ArrayList<>();
        String todayDate = dateFormat.format(helper.getYesterdayDate());

        // Extreme Case 1: A00062 arrives at 23:23 PM for 00:00-08:00 shift
        logs.add(createLog("A00062", todayDate, "23:23:45", "SLT-HQ01", "IN        "));
        logs.add(createLog("A00062", todayDate, "00:05:12", "SLT-HQ01", "IN        ")); // Official start
        logs.add(createLog("A00062", todayDate, "03:30:33", "SLT-HQ02", "OUT       ")); // Break
        logs.add(createLog("A00062", todayDate, "04:15:22", "SLT-HQ02", "IN        ")); // Return
        logs.add(createLog("A00062", todayDate, "08:10:55", "SLT-HQ01", "OUT       ")); // End

        // Extreme Case 2: A00065 arrives at 07:45 AM for 08:00-16:00 shift
        logs.add(createLog("A00065", todayDate, "07:45:30", "SLT-HQ03", "IN        "));
        logs.add(createLog("A00065", todayDate, "08:00:00", "SLT-HQ03", "IN        ")); // Exact start
        logs.add(createLog("A00065", todayDate, "12:00:15", "SLT-HQ01", "OUT       ")); // Lunch
        logs.add(createLog("A00065", todayDate, "13:00:45", "SLT-HQ01", "IN        ")); // Return
        logs.add(createLog("A00065", todayDate, "16:15:30", "SLT-HQ03", "OUT       ")); // End

        // Extreme Case 3: A00048 arrives at 15:30 PM for 16:00-24:00 shift
        logs.add(createLog("A00048", todayDate, "15:30:18", "SLT-HQ02", "IN        "));
        logs.add(createLog("A00048", todayDate, "16:00:05", "SLT-HQ02", "IN        ")); // Official start
        logs.add(createLog("A00048", todayDate, "20:30:44", "SLT-HQ03", "OUT       ")); // Dinner break
        logs.add(createLog("A00048", todayDate, "21:30:12", "SLT-HQ03", "IN        ")); // Return
        logs.add(createLog("A00048", todayDate, "23:55:59", "SLT-HQ02", "OUT       ")); // Late end

        accessLogRepo.saveAll(logs);

        System.out.println("Generated " + logs.size() + " extreme edge case test logs");
        System.out.println("A00062: Night shift (00:00-08:00) with 23:23 early arrival");
        System.out.println("A00065: Day shift (08:00-16:00) with 07:45 early arrival");
        System.out.println("A00048: Evening shift (16:00-24:00) with 15:30 early arrival");
    }

    // ORIGINAL METHODS FROM YOUR CODE
    @Test
    public void generateFullyRandomShiftBasedAccessLogs() {

        List<AccessLogEntity> logs = new ArrayList<>();
        String todayDate = dateFormat.format(helper.getYesterdayDate());

        // Target employee IDs
        String[] TARGET_EMPLOYEES = {"A00048", "A00046", "A00022", "A00065", "A00039","A00062"};

        // Shift types
        enum ShiftType {
            NIGHT(0, 8),    // 00:00 - 08:00
            DAY(8, 16),     // 08:00 - 16:00
            EVENING(16, 24); // 16:00 - 24:00

            final int startHour, endHour;
            ShiftType(int start, int end) { this.startHour = start; this.endHour = end; }
        }

        // Randomly assign each employee to a shift
        for (String empId : TARGET_EMPLOYEES) {
            ShiftType assignedShift = ShiftType.values()[random.nextInt(ShiftType.values().length)];

            // Random chance of showing up to work (70-95%)
            double attendanceRate = 0.7 + random.nextDouble() * 0.25;

            if (random.nextDouble() < attendanceRate) {
                generateRandomShiftLogs(logs, empId, todayDate, assignedShift);
            }
        }

        // Save all logs
        accessLogRepo.saveAll(logs);

        System.out.println("Generated " + logs.size() + " fully randomized shift-based access logs for date: " + todayDate);
    }

    private void generateRandomShiftLogs(List<AccessLogEntity> logs, String empId, String todayDate, Object shift) {

        // Cast shift to get access to enum values
        int shiftStartHour, shiftEndHour;
        if (shift.toString().equals("NIGHT")) {
            shiftStartHour = 0; shiftEndHour = 8;
        } else if (shift.toString().equals("DAY")) {
            shiftStartHour = 8; shiftEndHour = 16;
        } else { // EVENING
            shiftStartHour = 16; shiftEndHour = 24;
        }

        // Random shift start behavior
        double shiftStartChance = 0.6 + random.nextDouble() * 0.35; // 60-95%
        if (random.nextDouble() < shiftStartChance) {
            String startTime = generateRandomTimeInRange(
                    shiftStartHour,
                    Math.min(shiftStartHour + 2, shiftEndHour), // Up to 2 hours into shift
                    random.nextBoolean() ? -30 : 0, // Sometimes arrive 30 min early
                    60 // Or up to 1 hour late
            );

            logs.add(createLog(empId, todayDate, startTime, randomTerminal(), "IN        "));
        }

        // Random number of mid-shift activities (0-4)
        int midShiftActivities = random.nextInt(5);
        for (int i = 0; i < midShiftActivities; i++) {

            // Random time during shift
            String outTime = generateRandomTimeInRange(
                    shiftStartHour + 1,
                    shiftEndHour - 1,
                    0, 0
            );

            String inTime = generateRandomTimeInRange(
                    Integer.parseInt(outTime.split(":")[0]),
                    shiftEndHour - 1,
                    15, // At least 15 min break
                    180 // Up to 3 hour break
            );

            // Only add if the sequence makes sense
            if (compareTimeStrings(outTime, inTime) < 0) {
                logs.add(createLog(empId, todayDate, outTime, randomTerminal(), "OUT       "));
                logs.add(createLog(empId, todayDate, inTime, randomTerminal(), "IN        "));
            }
        }

        // Random shift end behavior
        double shiftEndChance = 0.5 + random.nextDouble() * 0.4; // 50-90%
        if (random.nextDouble() < shiftEndChance) {
            String endTime = generateRandomTimeInRange(
                    Math.max(shiftEndHour - 2, shiftStartHour),
                    shiftEndHour,
                    -60, // Up to 1 hour early
                    120  // Or up to 2 hours late
            );

            logs.add(createLog(empId, todayDate, endTime, randomTerminal(), "OUT       "));
        }

        // Random chance of weird/erratic entries (5-15%)
        double erraticChance = 0.05 + random.nextDouble() * 0.1;
        if (random.nextDouble() < erraticChance) {
            generateErraticEntries(logs, empId, todayDate, shiftStartHour, shiftEndHour);
        }
    }

    private void generateErraticEntries(List<AccessLogEntity> logs, String empId, String todayDate, int shiftStart, int shiftEnd) {
        // Generate 1-3 random entries at unusual times
        int erraticCount = 1 + random.nextInt(3);

        for (int i = 0; i < erraticCount; i++) {
            // Completely random time in 24-hour period
            String randomTime = generateRandomTimeInRange(0, 24, 0, 0);
            String randomAction = random.nextBoolean() ? "IN        " : "OUT       ";

            logs.add(createLog(empId, todayDate, randomTime, randomTerminal(), randomAction));
        }
    }

    private String generateRandomTimeInRange(int baseStartHour, int baseEndHour, int minOffsetMinutes, int maxOffsetMinutes) {
        // Handle hour range
        int startHour = Math.max(0, Math.min(23, baseStartHour));
        int endHour = Math.max(startHour + 1, Math.min(24, baseEndHour));

        // Random hour in range
        int hour = startHour + random.nextInt(endHour - startHour);

        // Handle 24-hour wraparound
        if (hour >= 24) hour = hour % 24;

        // Random base minute
        int minute = random.nextInt(60);

        // Apply random offset
        if (maxOffsetMinutes > 0 || minOffsetMinutes != 0) {
            int offsetRange = maxOffsetMinutes - minOffsetMinutes;
            int offset = minOffsetMinutes + (offsetRange > 0 ? random.nextInt(offsetRange + 1) : 0);

            minute += offset;

            // Handle minute overflow/underflow
            while (minute >= 60) {
                minute -= 60;
                hour = (hour + 1) % 24;
            }
            while (minute < 0) {
                minute += 60;
                hour = hour - 1;
                if (hour < 0) hour = 23;
            }
        }

        int second = random.nextInt(60);

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private int compareTimeStrings(String time1, String time2) {
        // Simple time comparison for HH:MM:SS format
        return time1.compareTo(time2);
    }

    private AccessLogEntity createLog(String empId, String date, String time, String terminal, String inOut) {
        // Randomly vary terminal padding
        int terminalPadding = 15 + random.nextInt(10); // 15-24 characters
        String paddedTerminal = String.format("%-" + terminalPadding + "s", terminal);

        // Randomly vary time padding
        int timePadding = 8 + random.nextInt(5); // 8-12 characters
        String paddedTime = String.format("%-" + timePadding + "s", time);

        // Random read status (mostly 0, sometimes 1)
        String readStatus = random.nextDouble() < 0.85 ? "0" : "1";

        // Random processed status (mostly 1, sometimes 0)
        int processed = random.nextDouble() < 0.9 ? 1 : 0;

        return AccessLogEntity.builder()
                .employeeId(empId)
                .logDate(date)
                .logTime(paddedTime)
                .terminalId(paddedTerminal)
                .inOut(inOut)
                .readStatus(readStatus)
                .processed(processed)
                .etlRunTime(generateRandomEtlRunTime(date))
                .build();
    }

    private String randomTerminal() {
        return TERMINALS[random.nextInt(TERMINALS.length)];
    }

    private Date generateRandomEtlRunTime(String logDate) {
        try {
            Date date = dateFormat.parse(logDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // Completely random ETL time - could be same day, next day, or even future
            cal.add(Calendar.HOUR, random.nextInt(72)); // 0-72 hours later
            cal.add(Calendar.MINUTE, random.nextInt(60));
            cal.add(Calendar.SECOND, random.nextInt(60));

            return cal.getTime();
        } catch (Exception e) {
            return new Date();
        }
    }

    // Original test method for reference
    @Test
    public void generateRandomAccessLogs() {

        List<AccessLogEntity> logs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        String todayDate = dateFormat.format(helper.getYesterdayDate());

        String[] EMPLOYEE_IDS = {"A00012", "A00015", "A00020", "A00025", "A00029"};

        // For each employee
        for (String empId : EMPLOYEE_IDS) {
            // 80% chance of having morning entry
            if (random.nextDouble() < 0.8) {
                logs.add(createOriginalLog(empId, todayDate,
                        generateMorningTime(),
                        randomTerminal(),
                        "IN        "));
            }

            // 70% chance of having evening entry
            if (random.nextDouble() < 0.7) {
                logs.add(createOriginalLog(empId, todayDate,
                        generateEveningTime(),
                        randomTerminal(),
                        "OUT       "));
            }

            // 20% chance of having additional entries (lunch breaks, etc)
            if (random.nextDouble() < 0.2) {
                logs.add(createOriginalLog(empId, todayDate,
                        generateLunchOutTime(),
                        randomTerminal(),
                        "OUT       "));

                logs.add(createOriginalLog(empId, todayDate,
                        generateLunchInTime(),
                        randomTerminal(),
                        "IN        "));
            }
        }

        // Save all logs
        accessLogRepo.saveAll(logs);
    }

    private AccessLogEntity createOriginalLog(String empId, String date, String time,
                                              String terminal, String inOut) {
        // Pad terminal to 20 characters
        String paddedTerminal = String.format("%-20s", terminal);

        return AccessLogEntity.builder()
                .employeeId(empId)
                .logDate(date)
                .logTime(String.format("%-10s", time)) // Pad time to 10 chars
                .terminalId(paddedTerminal)
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
}*/
