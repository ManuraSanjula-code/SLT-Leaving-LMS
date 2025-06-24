package com.slt.radio.rosterservice.Service.LMS;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.slt.radio.rosterservice.Model.One.LMS.AccessLog;
import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Repo.AccessLogRepository;
import com.slt.radio.rosterservice.Repo.DutyRosterRepository;
import com.slt.radio.rosterservice.Utils.Helper;
import com.slt.radio.rosterservice.Utils.TokenCreator;
import com.slt.radio.rosterservice.feign_client.LMSClient;
import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
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
    private final Helper helper;

    @Autowired
    private LMSClient lmsClient;

    @Autowired
    private TokenCreator tokenCreator;

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

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private String getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date date = removeTimeFromDate(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        return formatter.format(date);
    }

    @Scheduled(cron = "00 00 03  * * ?")
    public void getLogs() throws NoSuchAlgorithmException, JOSEException {

        log.info("Starting getting logs form the lms server");

        SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
        String token = "Bearer " + tokenCreator.encryptToken(signToken);

        List<AccessLogArchiveRest> allAccessLogsToday = lmsClient.getAllAccessLogsToday(helper.getFormattedYesterdayDate(), token);
        for (AccessLogArchiveRest lms : allAccessLogsToday) {
            try {
                log.info("Processing LMS data: {}", lms);

                AccessLog accessLog = new AccessLog(lms);
                log.info("Created AccessLog: {}", accessLog);

                if (accessLog.getEmployeeId() == null || accessLog.getLogDate() == null) {
                    log.warn("Skipping invalid access log - missing required fields: {}", accessLog);
                    continue;
                }

                log.info("Attempting to save AccessLog to database...");
                AccessLog saved = accessLogRepository.save(accessLog);

                if (saved != null && saved.getId() != null) {
                    log.info("Successfully saved AccessLog with ID: {}", saved.getId());
                } else {
                    log.error("Save returned null or without ID: {}", saved);
                }

                if (saved != null && saved.getId() != null) {
                    Optional<AccessLog> fetched = accessLogRepository.findById(saved.getId());
                    if (fetched.isPresent()) {
                        log.info("Verification successful - record exists in database");
                    } else {
                        log.error("Verification failed - record not found in database after save");
                    }
                }

            } catch (Exception e) {
                log.error("Exception during save operation for LMS data: {}", lms, e);
            }
        }
    }

    @Scheduled(cron = "00 30 03 * * ?")
    public void syncAccessLogsAndProcessAttendance() {
        log.info("Starting daily sync of access logs and attendance processing");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.format(DATE_FORMATTER);

        try {
            List<AccessLog> accessLogs = fetchAccessLogsFromSLT(getYesterdayDate());
            attendanceService.processAccessLogs(accessLogs);
            attendanceService.processAttendanceForDate(yesterdayStr);

            attendanceService.processDutyAttendances();

            log.info("Completed daily sync of access logs and attendance processing");
        } catch (Exception e) {
            log.error("Error in daily sync process", e);
        }
    }


    private List<AccessLog> fetchAccessLogsFromSLT(String dateStr) {
        log.info("Fetching access logs from SLT server for date: {}", dateStr);
        return accessLogRepository.findByLogDate(dateStr);
    }


    public void processAttendanceForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            log.info("Processing attendance for date: {}", dateStr);

            try {
                attendanceService.processAttendanceForDate(dateStr);
            } catch (Exception e) {
                log.error("Error processing attendance for date: {}", dateStr, e);
            }

            currentDate = currentDate.plusDays(1);
        }
    }
}
