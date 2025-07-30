package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.InOutType;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.AccessLogRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogServiceImpl.class);
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
    private static final LocalTime NOON = LocalTime.NOON;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String ALTERNATE_DATE_FORMAT = "yyyy/MM/dd";
    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String FALLBACK_TIME = "00:00:00";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat(ALTERNATE_DATE_FORMAT);

    @Autowired
    private AccessLogRepo accessLogRepository;

    @Autowired
    private InOutRepo inOutRepository;

    @Autowired
    private Helper helper;

    @Override
    @Transactional
    public void processLogEntry(boolean swap) {
        logger.info("Starting batch log processing for {} logs", swap ? "today" : "yesterday");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            List<AccessLogEntity> logs = swap ?
                    accessLogRepository.findByTodayLogs() :
                    accessLogRepository.findByYesterdayLogs();

            if (logs.isEmpty()) {
                logger.info("No logs found for {} processing", swap ? "today" : "yesterday");
                return;
            }

            logger.info("Found {} logs to process for {}", logs.size(), swap ? "today" : "yesterday");

            for (AccessLogEntity log : logs) {
                try {
                    processAccessLog(log, swap);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    errorLogger.error("Failed to process log entry with ID: {}. Error: {}",
                            log.getEmployeeId(), e.getMessage());
                    logger.debug("Stack trace for failed log entry {}:", log.getEmployeeId(), e);
                }
            }

            logger.info("Batch processing completed for {}. Success: {}, Failures: {}",
                    swap ? "today" : "yesterday", successCount.get(), failureCount.get());
        } catch (Exception e) {
            errorLogger.error("Unexpected error in processLogEntry for {} logs: {}",
                    swap ? "today" : "yesterday", e.getMessage());
            logger.debug("Stack trace for processLogEntry:", e);
        }
    }

    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void processAccessLog(AccessLogEntity log, boolean swap) {
        if (log == null) {
            errorLogger.error("Null log entry received");
            return;
        }

        try {
            String logDate = getValidLogDate(log);
            String logTime = getValidLogTime(log);

            Date punchDate = parseDateSafely(logDate);
            Time punchTime = parseTime(logTime);

            if (punchDate == null || punchTime == null) {
                errorLogger.error("Invalid date/time for employee {}. Date: {}, Time: {}",
                        log.getEmployeeId(), logDate, logTime);
                return;
            }

            logger.debug("Processing log - EmployeeID: {}, Date: {}, Time: {}, Inout: {}",
                    log.getEmployeeId(), punchDate, punchTime, log.getInOut());

            saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(),
                    log.getTerminalId(), log, swap);
        } catch (Exception e) {
            errorLogger.error("Error processing log for employee {}. Error: {}",
                    log.getEmployeeId(), e.getMessage());
            logger.debug("Stack trace for employee {}:", log.getEmployeeId(), e);
        }
    }

    private String getValidLogDate(AccessLogEntity log) {
        try {
            if (StringUtils.isNotBlank(log.getLogDate())) {
                return log.getLogDate();
            }
            logger.warn("Empty log date for employee {}, using fallback date", log.getEmployeeId());
            return dateFormat.format(getFallbackDate(false)); // Default to current date format
        } catch (Exception e) {
            errorLogger.error("Error getting log date for employee {}, using fallback. Error: {}",
                    log.getEmployeeId(), e.getMessage());
            return dateFormat.format(getFallbackDate(false));
        }
    }

    private String getValidLogTime(AccessLogEntity log) {
        try {
            if (StringUtils.isNotBlank(log.getLogTime())) {
                return log.getLogTime();
            }
            logger.warn("Empty log time for employee {}, using current time", log.getEmployeeId());
            return timeFormat.format(new Date());
        } catch (Exception e) {
            errorLogger.error("Error getting log time for employee {}, using fallback. Error: {}",
                    log.getEmployeeId(), e.getMessage());
            return FALLBACK_TIME;
        }
    }

    private Date getFallbackDate(boolean swap) {
        try {
            return swap ? helper.removeTimeFromDate(new Date()) : helper.getYesterdayDate();
        } catch (Exception e) {
            errorLogger.error("Error getting fallback date, using current date. Error: {}", e.getMessage());
            return new Date();
        }
    }

    private Date parseDateSafely(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            logger.warn("Empty date string received, using fallback date");
            return getFallbackDate(false);
        }

        try {
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                logger.debug("Failed to parse date in primary format, trying alternate format");
                try {
                    return inputDateFormat.parse(dateString);
                } catch (ParseException e2) {
                    errorLogger.error("Failed to parse date in all formats: {}", dateString);
                    return getFallbackDate(false);
                }
            }
        } catch (Exception e) {
            errorLogger.error("Unexpected error parsing date: {}. Using fallback. Error: {}",
                    dateString, e.getMessage());
            return getFallbackDate(false);
        }
    }

    @Override
    public Time parseTime(String timeString) {
        if (StringUtils.isBlank(timeString)) {
            logger.warn("Empty time string received, using fallback time");
            return new Time(new Date().getTime());
        }

        try {
            return new Time(timeFormat.parse(timeString).getTime());
        } catch (ParseException e) {
            errorLogger.error("Failed to parse time: {}. Using fallback. Error: {}",
                    timeString, e.getMessage());
            try {
                return new Time(timeFormat.parse(FALLBACK_TIME).getTime());
            } catch (ParseException e2) {
                return new Time(new Date().getTime());
            }
        } catch (Exception e) {
            errorLogger.error("Unexpected error parsing time: {}. Using fallback. Error: {}",
                    timeString, e.getMessage());
            return new Time(new Date().getTime());
        }
    }

    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    protected void saveInOutRecord(String logDate, Time punchTime, String employeeID, String inout,
                                   String terminalId, AccessLogEntity log, boolean swap) {
        if (StringUtils.isBlank(employeeID) || StringUtils.isBlank(inout) || StringUtils.isBlank(terminalId)) {
            errorLogger.error("Missing required fields for employee {}. EmployeeID: {}, Inout: {}, Terminal: {}",
                    employeeID, employeeID, inout, terminalId);
            return;
        }

        try {
            InOutEntity inOut = new InOutEntity();
            inOut.setTerminalId(terminalId);
            LocalTime punchLocalTime = punchTime != null ? punchTime.toLocalTime() : LocalTime.MIDNIGHT;
            boolean isMorning = punchLocalTime.isBefore(NOON);

            Date date = parseDateSafely(logDate);
            if (date == null) {
                date = getFallbackDate(swap);
            }

            // Check for existing records
            List<InOutEntity> existingRecords = inOutRepository
                    .findByEmployeeIdAndPunchTimeAndPunchTypeTimeAndTerminalId(
                            employeeID,
                            date,
                            punchTime,
                            terminalId);

            if (!existingRecords.isEmpty()) {
                logger.debug("Duplicate record found for employee: {}, date: {}, terminal: {}",
                        employeeID, date, terminalId);
                return;
            }

            inOut.setEmployeeId(employeeID);
            inOut.setDate(getFallbackDate(swap));
            inOut.setEtlRunTime(new Date());

            String normalizedInout = inout.trim().toUpperCase();

            switch (normalizedInout) {
                case "IN":
                    inOut.setInOutValue(1);
                    inOut.setInOutType(isMorning ? InOutType.MORNING_IN : InOutType.EVENING_IN);
                    break;
                case "OUT":
                    inOut.setInOutValue(0);
                    inOut.setInOutType(isMorning ? InOutType.MORNING_OUT : InOutType.EVENING_OUT);
                    break;
                default:
                    errorLogger.error("Invalid inout value for employee {}. Value: {}", employeeID, inout);
                    return;
            }

            inOut.setPunchTypeTime(punchTime != null ? punchTime : new Time(0));
            inOut.setPunchTime(date != null ? date : getFallbackDate(swap));
            inOut.setCreatedDate(new Date());
            inOut.setUpdatedDate(new Date());

            try {
                InOutEntity savedInOut = inOutRepository.save(inOut);
                logger.debug("Successfully saved InOut record for employee: {}", employeeID);
            } catch (DataIntegrityViolationException e) {
                logger.warn("Duplicate entry detected for employee: {}, date: {}", employeeID, date);
            } catch (DataAccessException e) {
                errorLogger.error("Database error for employee {}. Error: {}", employeeID, e.getMessage());
                throw e;
            } catch (Exception e) {
                errorLogger.error("Unexpected error saving record for employee {}. Error: {}",
                        employeeID, e.getMessage());
            }
        } catch (Exception e) {
            errorLogger.error("Error saving InOut record for employee {}. Error: {}",
                    employeeID, e.getMessage());
            logger.debug("Stack trace for employee {}:", employeeID, e);
        }
    }

    @Override
    public void main(boolean swap) {
        logger.info("Starting main access log processing for {} logs", swap ? "today" : "yesterday");
        long startTime = System.currentTimeMillis();

        try {
            processLogEntry(swap);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Main processing completed for {} in {} ms",
                    swap ? "today" : "yesterday", duration);
        } catch (Exception e) {
            errorLogger.error("Error in main processing for {} logs: {}",
                    swap ? "today" : "yesterday", e.getMessage());
            logger.debug("Stack trace for main processing:", e);
        }
    }

    @Override
    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void processLogEntry(AccessLogEntity log) {
        processAccessLog(log, false);
    }
}