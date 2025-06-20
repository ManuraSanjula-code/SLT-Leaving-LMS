package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.InOutType;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.AccessLogRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private AccessLogRepo accessLogRepository;

    @Autowired
    private InOutRepo inOutRepository;

    @Autowired
    private Helper helper;

    @Override
    @Transactional
    public void processLogEntry() {
        logger.info("Starting batch log processing");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            List<AccessLogEntity> logs = accessLogRepository.findByYesterdayLogs();

            if (logs.isEmpty()) {
                logger.info("No logs found for processing");
                return;
            }

            logger.info("Found {} logs to process", logs.size());

            for (AccessLogEntity log : logs) {
                try {
                    processAccessLog(log);
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
    public void processAccessLog(AccessLogEntity log) {
        if (log == null) {
            IllegalArgumentException iae = new IllegalArgumentException("Log entry cannot be null");
            logger.error("Null log entry received", iae);
            throw iae;
        }

        try {
            String logDate = log.getLogDate() != null ? log.getLogDate() : currentDateFormat.format(new Date());
            Date punchDate = parseDate(logDate);
            Time punchTime = parseTime(log.getLogTime());

            logger.debug("Processing log - Date: {}, Time: {}, EmployeeID: {}, Inout: {}",
                    punchDate, punchTime, log.getEmployeeId(), log.getInOut());

            saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(), log.getTerminalId(), log);
        } catch (Exception e) {
            logger.error("Error processing log for employee {}", log.getEmployeeId(), e);
            throw new RuntimeException("Log processing failed", e);
        }
    }

    @Override
    public void main() {
        logger.info("Starting main access log processing");
        long startTime = System.currentTimeMillis();

        try {
            processLogEntry();
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
        if (log == null) {
            IllegalArgumentException iae = new IllegalArgumentException("Log entry cannot be null");
            logger.error("Null log entry received", iae);
            throw iae;
        }

        try {
            String logDate = log.getLogDate() != null ? log.getLogDate() : currentDateFormat.format(new Date());
            Date punchDate = parseDate(logDate);
            Time punchTime = parseTime(log.getLogTime());

            logger.debug("Processing log entry - Date: {}, Time: {}, EmployeeID: {}, Inout: {}",
                    punchDate, punchTime, log.getEmployeeId(), log.getInOut());
            saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(), log.getTerminalId(), log);
        } catch (Exception e) {
            logger.error("Error processing log entry for employee {}", log.getEmployeeId(), e);
            throw new RuntimeException("Log processing failed", e);
        }
    }

    private Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("Date string cannot be null or empty");
            logger.error("Invalid date string", iae);
            throw iae;
        }

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.error("Failed to parse date: {}", dateString, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error parsing date: {}", dateString, e);
            throw new RuntimeException("Date parsing failed", e);
        }
    }

    @Override
    public Time parseTime(String timeString) throws ParseException {
        if (timeString == null || timeString.trim().isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("Time string cannot be null or empty");
            logger.error("Invalid time string", iae);
            throw iae;
        }

        try {
            return new Time(timeFormat.parse(timeString).getTime());
        } catch (ParseException e) {
            logger.error("Failed to parse time: {}", timeString, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error parsing time: {}", timeString, e);
            throw new RuntimeException("Time parsing failed", e);
        }
    }

    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    protected void saveInOutRecord(String logDate, Time punchTime, String employeeID, String inout,
                                   String terminalId, AccessLogEntity log) {
        if (employeeID == null || employeeID.trim().isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("Employee ID cannot be null or empty");
            logger.error("Invalid employee ID", iae);
            throw iae;
        }

        if (inout == null || inout.trim().isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("Inout value cannot be null or empty");
            logger.error("Invalid inout value", iae);
            throw iae;
        }

        if (terminalId == null || terminalId.trim().isEmpty()) {
            IllegalArgumentException iae = new IllegalArgumentException("Terminal ID cannot be null or empty");
            logger.error("Invalid terminal ID", iae);
            throw iae;
        }

        try {
            InOutEntity inOut = new InOutEntity();
            inOut.setTerminalId(terminalId);
            LocalTime punchLocalTime = LocalTime.parse(punchTime.toString());
            boolean isMorning = punchLocalTime.isBefore(NOON);

            Date date = inputDateFormat.parse(logDate);

            inOut.setEmployeeId(employeeID);
            inOut.setDate(helper.getYesterdayDate());
            inOut.setEtlRunTime(new Date());
            String normalizedInout = inout.trim().toUpperCase();

            switch (normalizedInout) {
                case "IN":
                    inOut.setInOutValue(1);
                    if (isMorning) {
                        inOut.setInOutType(InOutType.MORNING_IN);
                    } else {
                        inOut.setInOutType(InOutType.EVENING_IN);
                    }
                    inOut.setPunchTypeTime(punchTime);
                    inOut.setPunchTime(date);
                    logger.debug("Prepared IN record for employee: {}, date: {}, time: {}",
                            employeeID, logDate, punchTime);
                    break;

                case "OUT":
                    inOut.setInOutValue(0);
                    if (isMorning) {
                        inOut.setInOutType(InOutType.MORNING_OUT);
                    } else {
                        inOut.setInOutType(InOutType.EVENING_OUT);
                    }
                    inOut.setPunchTypeTime(punchTime);
                    inOut.setPunchTime(date);
                    logger.debug("Prepared OUT record for employee: {}, date: {}, time: {}",
                            employeeID, logDate, punchTime);
                    break;

                default:
                    IllegalArgumentException iae = new IllegalArgumentException(
                            "Invalid inout value. Expected 'IN' or 'OUT', got: " + inout);
                    logger.error("Invalid inout value: {} for employee: {}", inout, employeeID, iae);
                    throw iae;
            }

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

            inOut.setCreatedDate(new Date());
            inOut.setUpdatedDate(new Date());

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
            } catch (Exception e) {
                logger.error("Unexpected error while saving record for employee {}", employeeID, e);
                throw new RuntimeException("Record save failed", e);
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