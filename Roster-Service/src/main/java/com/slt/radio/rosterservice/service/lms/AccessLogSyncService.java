package com.slt.radio.rosterservice.service.lms;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.slt.radio.rosterservice.document.one.lms.AccessLog;
import com.slt.radio.rosterservice.repo.AccessLogRepository;
import com.slt.radio.rosterservice.repo.DutyRosterRepository;
import com.slt.radio.rosterservice.utils.Helper;
import com.slt.radio.rosterservice.utils.TokenCreator;
import com.slt.radio.rosterservice.feign_client.LMSClient;
import com.slt.radio.rosterservice.feign_client.model.AccessLogArchiveRest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogSyncService {

    private final AccessLogRepository accessLogRepository;
    private final AttendanceService attendanceService;
    private final DutyRosterRepository dutyRosterRepository;
    private final Helper helper;
    private final TokenCreator tokenCreator;

    @Autowired
    private LMSClient lmsClient;

    private static final DateTimeFormatter DATE_FORMATTER_ = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");   
     private static final String LMS_SERVICE_USER = "lms@slt.com";

    @Scheduled(cron = "00 00 03  * * ?")
    public void getLogs() {
        log.info("==== Starting daily access log sync process ====");

        try {
            String yesterdayDate = helper.getFormattedYesterdayDate();
            log.info("Fetching access logs for date: {}", yesterdayDate);

            String token = createAuthToken();
            List<AccessLogArchiveRest> accessLogs = fetchAccessLogsWithRetry(yesterdayDate, token);

            if (CollectionUtils.isEmpty(accessLogs)) {
                log.warn("No access logs received for date: {}", yesterdayDate);
                return;
            }

            log.info("Processing {} access log entries", accessLogs.size());
            processAndSaveAccessLogs(accessLogs);

        } catch (NoSuchAlgorithmException | JOSEException e) {
            log.error("Token creation failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error in getLogs scheduler", e);
        } finally {
            log.info("==== Completed daily access log sync process ====");
        }
    }

    private String createAuthToken() throws NoSuchAlgorithmException, JOSEException {
        SignedJWT signToken = tokenCreator.createSignedJWT(LMS_SERVICE_USER);
        return "Bearer " + tokenCreator.encryptToken(signToken);
    }

    private List<AccessLogArchiveRest> fetchAccessLogsWithRetry(String date, String token) {
        try {
            List<AccessLogArchiveRest> logs = lmsClient.getAllAccessLogsToday(date, token);
            return Optional.ofNullable(logs).orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to fetch access logs from LMS service (Attempt 1/3): {}", e.getMessage());
            return attemptRetryFetch(date, token, e);
        }
    }

    private List<AccessLogArchiveRest> attemptRetryFetch(String date, String token, Exception originalException) {
        for (int attempt = 2; attempt <= 3; attempt++) {
            try {
                Thread.sleep(1000 * attempt);
                List<AccessLogArchiveRest> logs = lmsClient.getAllAccessLogsToday(date, token);
                if (!CollectionUtils.isEmpty(logs)) {
                    log.info("Successfully fetched access logs on attempt {}", attempt);
                    return logs;
                }
            } catch (Exception e) {
                log.error("Failed to fetch access logs from LMS service (Attempt {}/3): {}",
                        attempt, e.getMessage());
            }
        }
        log.error("All retry attempts failed. Last error: {}", originalException.getMessage());
        return Collections.emptyList();
    }

    private void processAndSaveAccessLogs(List<AccessLogArchiveRest> accessLogs) {
        Map<Boolean, List<AccessLog>> partitionedLogs = accessLogs.stream()
                .filter(Objects::nonNull)
                .map(this::createAccessLog)
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(
                        log -> isValidAccessLog(log) && !helper.isDuplicateAccessLog(log)
                ));

        List<AccessLog> validLogs = partitionedLogs.get(true);
        List<AccessLog> invalidLogs = partitionedLogs.get(false);

        logInvalidAccessLogs(invalidLogs);
        saveValidAccessLogs(validLogs);
    }

    private AccessLog createAccessLog(AccessLogArchiveRest lms) {
        try {
            return new AccessLog(lms);
        } catch (Exception e) {
            log.error("Failed to create AccessLog from LMS data: {}. Error: {}", lms, e.getMessage());
            return null;
        }
    }

    private boolean isValidAccessLog(AccessLog accessLog) {
        return accessLog != null
                && StringUtils.isNotBlank(accessLog.getEmployeeId())
                && accessLog.getLogDate() != null;
    }

    private void logInvalidAccessLogs(List<AccessLog> invalidLogs) {
        if (!CollectionUtils.isEmpty(invalidLogs)) {
            log.warn("Found {} invalid/duplicate access logs:", invalidLogs.size());
            invalidLogs.forEach(accessLog ->
                    log.debug("Invalid log - EmployeeID: {}, Date: {}",
                            accessLog.getEmployeeId(), accessLog.getLogDate()));
        }
    }

    private void saveValidAccessLogs(List<AccessLog> validLogs) {
        if (CollectionUtils.isEmpty(validLogs)) {
            log.info("No valid access logs to save");
            return;
        }

        int batchSize = 100;
        for (int i = 0; i < validLogs.size(); i += batchSize) {
            List<AccessLog> batch = validLogs.subList(i, Math.min(i + batchSize, validLogs.size()));
            try {
                List<AccessLog> savedLogs = accessLogRepository.saveAll(batch);
                log.info("Successfully saved batch of {} access logs ({} to {})",
                        savedLogs.size(), i, Math.min(i + batchSize, validLogs.size()) - 1);
            } catch (Exception e) {
                log.error("Failed to save batch of access logs ({} to {}): {}",
                        i, Math.min(i + batchSize, validLogs.size()) - 1, e.getMessage());
            }
        }
    }

    @Scheduled(cron = "00 30 03  * * ?")
    public void syncAccessLogsAndProcessAttendance() {
        log.info("Starting attendance processing for yesterday's logs");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            String yesterdayStr = yesterday.format(DATE_FORMATTER);

            List<AccessLog> accessLogs = fetchAccessLogsFromSLT(yesterdayStr);
            attendanceService.processAccessLogs(accessLogs);
            attendanceService.processAttendanceForDate(yesterdayStr);
            attendanceService.processDutyAttendances();

        } catch (Exception e) {
            log.error("Error in attendance processing", e);
        }
    }

    @Scheduled(cron = "00 00 04  * * ?")
    public void syncAccessLogsAndProcessAbsentAttendance() {
        try {
            attendanceService.processAbsentAttendance();
        } catch (Exception e) {
            log.error("Error in absent attendance processing", e);
        }
    }

    private List<AccessLog> fetchAccessLogsFromSLT(String dateStr) {
        try {
            List<AccessLog> logs = accessLogRepository.findByLogDate(dateStr);
            return CollectionUtils.isEmpty(logs) ? Collections.emptyList() : logs;
        } catch (Exception e) {
            log.error("Failed to fetch access logs from database for date: {}", dateStr, e);
            return Collections.emptyList();
        }
    }

    public void processAttendanceForDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Processing attendance for date range: {} to {}", startDate, endDate);

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            try {
                attendanceService.processAttendanceForDate(dateStr);
            } catch (Exception e) {
                log.error("Error processing attendance for date: {}", dateStr, e);
            }
            currentDate = currentDate.plusDays(1);
        }
    }
}