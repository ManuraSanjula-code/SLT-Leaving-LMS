package com.slt.radio.rosterservice.Service.LMS;

import com.slt.radio.rosterservice.Model.One.LMS.AccessLog;
import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Repo.AccessLogRepository;
import com.slt.radio.rosterservice.Repo.DutyRosterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogSyncService {

    private final AccessLogRepository accessLogRepository;
    private final AttendanceService attendanceService;
    private final DutyRosterRepository dutyRosterRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private Optional<DutyRoster> findCurrentWeekDutyRoster() {
        LocalDate currentDate = LocalDate.now();
        return dutyRosterRepository.findByWeekStartingDate(currentDate);
    }

    private Date removeTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);

        // Reset hour, minute, second and millisecond
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private String getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date date = removeTimeFromDate(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
    /**
     * Scheduled job to sync access logs from SLT server and process attendance
     * Runs daily at 1:00 AM - @Scheduled(cron = "0 0 1 * * ?")
     */
    @Scheduled(cron = "0 28 20 * * ?")
    public void syncAccessLogsAndProcessAttendance() {
        log.info("Starting daily sync of access logs and attendance processing");

        // Get yesterday's date
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.format(DATE_FORMATTER);

        try {
            List<AccessLog> accessLogs = fetchAccessLogsFromSLT(getYesterdayDate());
            findCurrentWeekDutyRoster().ifPresent(attendanceService::processAttendanceForDate);

            // Process access logs to create InOut records
            //attendanceService.processAccessLogs(accessLogs);

            // Process attendance for yesterday
            //attendanceService.processAttendanceForDate(yesterdayStr);

            log.info("Completed daily sync of access logs and attendance processing");
        } catch (Exception e) {
            log.error("Error in daily sync process", e);
        }
    }

    /**
     * Method to fetch access logs from SLT server
     * This is a placeholder method that should be implemented based on SLT API
     */
    private List<AccessLog> fetchAccessLogsFromSLT(String dateStr) {
        log.info("Fetching access logs from SLT server for date: {}", dateStr);
        return accessLogRepository.findByLogDate(dateStr);
    }

    /**
     * Manual method to process attendance for a specific date range
     * Useful for backfilling or correcting attendance data
     */
    public void processAttendanceForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            log.info("Processing attendance for date: {}", dateStr);

            try {
                // Process attendance for the date
                attendanceService.processAttendanceForDate(dateStr);
            } catch (Exception e) {
                log.error("Error processing attendance for date: {}", dateStr, e);
            }

            currentDate = currentDate.plusDays(1);
        }
    }
}