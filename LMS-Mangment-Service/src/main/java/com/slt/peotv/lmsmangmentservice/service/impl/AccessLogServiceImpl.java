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
    private static final LocalTime NOON = LocalTime.NOON;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";
    private static final String ALTERNATE_DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm:ss";

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
        logger.info("Starting batch log processing");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            List<AccessLogEntity> logs = swap ? 
                accessLogRepository.findByTodayLogs() : 
                accessLogRepository.findByYesterdayLogs();

            if (logs.isEmpty()) {
                logger.info("No logs found for processing");
                return;
            }

            logger.info("Found {} logs to process", logs.size());

            for (AccessLogEntity log : logs) {
                try {
                    processAccessLog(log, swap);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.error("Failed to process log entry with ID: {}", log.getEmployeeId(), e);
                }
            }

            logger.info("Batch processing completed. Success: {}, Failures: {}",
                    successCount.get(), failureCount.get());
        } catch (Exception e) {
            logger.error("Unexpected error in processLogEntry", e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void processAccessLog(AccessLogEntity log, boolean swap) {
        if (log == null) {
            IllegalArgumentException iae = new IllegalArgumentException("Log entry cannot be null");
            logger.error("Null log entry received", iae);
            throw iae;
        }

        try {
            String logDate = StringUtils.isNotBlank(log.getLogDate()) ? 
                log.getLogDate() : 
                dateFormat.format(new Date());
            
            String logTime = StringUtils.isNotBlank(log.getLogTime()) ?
                log.getLogTime() :
                timeFormat.format(new Date());

            Date punchDate = parseDate(logDate);
            Time punchTime = parseTime(logTime);

            logger.debug("Processing log - Date: {}, Time: {}, EmployeeID: {}, Inout: {}",
                    punchDate, punchTime, log.getEmployeeId(), log.getInOut());

            saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(), log.getTerminalId(), log, swap);
        } catch (Exception e) {
            logger.error("Error processing log for employee {}", log.getEmployeeId(), e);
            throw new RuntimeException("Log processing failed for employee: " + log.getEmployeeId(), e);
        }
    }

    @Override
    public void main(boolean swap) {
        logger.info("Starting main access log processing");
        long startTime = System.currentTimeMillis();

        try {
            processLogEntry(swap);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Main processing completed successfully in {} ms", duration);
        } catch (Exception e) {
            logger.error("Error in main access log processing", e);
            throw new RuntimeException("Main processing failed", e);
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

    private Date parseDate(String dateString) throws ParseException {
        if (StringUtils.isBlank(dateString)) {
            logger.warn("Empty or null date string received, using current date");
            return new Date();
        }

        try {
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                logger.debug("Failed to parse date in yyyy/MM/dd format, trying dd/MM/yyyy");
                return inputDateFormat.parse(dateString);
            }
        } catch (ParseException e) {
            logger.error("Failed to parse date: {}", dateString, e);
            throw new RuntimeException("Date parsing failed for string: " + dateString, e);
        }
    }

    @Override
    public Time parseTime(String timeString) throws ParseException {
        if (StringUtils.isBlank(timeString)) {
            logger.warn("Empty or null time string received, using current time");
            return new Time(new Date().getTime());
        }

        try {
            return new Time(timeFormat.parse(timeString).getTime());
        } catch (ParseException e) {
            logger.error("Failed to parse time: {}", timeString, e);
            throw new RuntimeException("Time parsing failed for string: " + timeString, e);
        }
    }

    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    protected void saveInOutRecord(String logDate, Time punchTime, String employeeID, String inout,
                                 String terminalId, AccessLogEntity log, boolean swap) {
        if (StringUtils.isBlank(employeeID)) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }

        if (StringUtils.isBlank(inout)) {
            throw new IllegalArgumentException("Inout value cannot be null or empty");
        }

        if (StringUtils.isBlank(terminalId)) {
            throw new IllegalArgumentException("Terminal ID cannot be null or empty");
        }

        try {
            InOutEntity inOut = new InOutEntity();
            inOut.setTerminalId(terminalId);
            LocalTime punchLocalTime = punchTime.toLocalTime();
            boolean isMorning = punchLocalTime.isBefore(NOON);

            Date date = inputDateFormat.parse(logDate);

            List<InOutEntity> existingRecords = inOutRepository
                    .findByEmployeeIdAndPunchTimeAndPunchTypeTimeAndTerminalId(
                            employeeID,
                            date,
                            punchTime,
                            terminalId);

            if (!existingRecords.isEmpty()) {
                logger.debug("Record already exists for employee: {}, punch_time: {}, punch_type_time: {}, terminal: {}. Skipping save.",
                        employeeID, date, punchTime, terminalId);
                return;
            }

            inOut.setEmployeeId(employeeID);
            inOut.setDate(swap ? helper.removeTimeFromDate(new Date()) : helper.getYesterdayDate());
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
                    throw new IllegalArgumentException(
                            "Invalid inout value. Expected 'IN' or 'OUT', got: " + inout);
            }

            inOut.setPunchTypeTime(punchTime);
            inOut.setPunchTime(date);
            inOut.setCreatedDate(new Date());
            inOut.setUpdatedDate(new Date());

            Optional<InOutEntity> existingRecord = inOutRepository
                    .findByEmployeeIdAndDateAndPunchTypeTimeAndTerminalId(
                            employeeID,
                            inOut.getDate(),
                            punchTime,
                            terminalId);

            if (existingRecord.isPresent()) {
                logger.debug("Record already exists for employee: {}, date: {}, time: {}, terminal: {}. Skipping save.",
                        employeeID, logDate, punchTime, terminalId);
                return;
            }

            try {
                InOutEntity savedInOut = inOutRepository.save(inOut);
                logger.debug("Successfully saved InOut record with ID: {} for employee: {}",
                        savedInOut.getId(), employeeID);
            } catch (DataIntegrityViolationException e) {
                logger.warn("Duplicate entry detected for employee: {}, date: {}, time: {}",
                        employeeID, logDate, punchTime, e);
            } catch (DataAccessException e) {
                logger.error("Database error while saving record for employee {}", employeeID, e);
                throw e;
            }
        } catch (ParseException e) {
            logger.error("Failed to parse date while saving InOut record for employee {}: {}",
                    employeeID, logDate, e);
            throw new RuntimeException("Date parsing error", e);
        } catch (Exception e) {
            logger.error("Unexpected error while saving InOut record for employee {}", employeeID, e);
            throw new RuntimeException("Failed to save InOut record", e);
        }
    }
}