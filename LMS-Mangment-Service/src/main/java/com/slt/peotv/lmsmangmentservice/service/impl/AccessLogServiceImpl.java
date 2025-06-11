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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogServiceImpl.class);
    private static final LocalTime NOON = LocalTime.NOON;
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
    public void processLogEntry() throws ParseException {
        logger.info("Processing log entries from archive");
        List<AccessLogEntity> logs = accessLogRepository.findAll();

        for (AccessLogEntity log : logs) {
            try {
                // Ensure we're working with a managed entity by refreshing it
                AccessLogEntity managedLog = accessLogRepository.findById(log.getId()).orElse(log);
                processAccessLog(managedLog);
            } catch (ParseException e) {
                logger.error("Failed to process log entry with ID: {}", log.getEmployeeId(), e);
                throw e;
            }
        }
    }

    @Transactional
    public void processAccessLog(AccessLogEntity log) throws ParseException {
        String logDate = log.getLogDate() != null ? log.getLogDate() : currentDateFormat.format(new Date());
        Date punchDate = parseDate(logDate);
        Time punchTime = parseTime(log.getLogTime());

        logger.debug("Processing log - Date: {}, Time: {}, EmployeeID: {}, Inout: {}",
                punchDate, punchTime, log.getEmployeeId(), log.getInOut());
        saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(), log.getTerminalId(), log);
    }

    @Override
    public void main() throws ParseException {
        logger.info("Starting main access log processing");
        prerequisite();
        processLogEntry();
    }

    @Override
    public void prerequisite() {
        logger.info("Running prerequisite checks");
    }

    @Override
    @Transactional
    public void processLogEntry(AccessLogEntity log) throws ParseException {
        String logDate = log.getLogDate() != null ? log.getLogDate() : currentDateFormat.format(new Date());
        Date punchDate = parseDate(logDate);
        Time punchTime = parseTime(log.getLogTime());

        logger.debug("Processing log entry - Date: {}, Time: {}, EmployeeID: {}, Inout: {}",
                punchDate, punchTime, log.getEmployeeId(), log.getInOut());
        saveInOutRecord(logDate, punchTime, log.getEmployeeId(), log.getInOut(), log.getTerminalId(), log);
    }

    private Date parseDate(String dateString) throws ParseException {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.error("Failed to parse date: {}", dateString, e);
            throw e;
        }
    }

    private Time parseTime(String timeString) throws ParseException {
        try {
            return new Time(timeFormat.parse(timeString).getTime());
        } catch (ParseException e) {
            logger.error("Failed to parse time: {}", timeString, e);
            throw e;
        }
    }

    @Transactional
    protected void saveInOutRecord(String logDate, Time punchTime, String employeeID, String inout, String terminalId, AccessLogEntity log) throws ParseException {
        if (inout == null) {
            logger.error("Inout value is null for employee: {}", employeeID);
            throw new IllegalArgumentException("Inout value cannot be null");
        }

        InOutEntity inOut = new InOutEntity();
        inOut.setTerminalId(terminalId);
        LocalTime punchLocalTime = LocalTime.parse(punchTime.toString());
        boolean isMorning = punchLocalTime.isBefore(NOON);

        try {
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
                        inOut.setPunchTypeTime(punchTime);
                        inOut.setPunchTime(date);
                    } else {
                        inOut.setInOutType(InOutType.EVENING_IN);
                        inOut.setPunchTypeTime(punchTime);
                        inOut.setPunchTime(date);
                    }
                    logger.debug("Prepared IN record for employee: {}, date: {}, time: {}",
                            employeeID, logDate, punchTime);
                    break;

                case "OUT":
                    inOut.setInOutValue(0);
                    if (isMorning) {
                        inOut.setInOutType(InOutType.MORNING_OUT);
                        inOut.setPunchTypeTime(punchTime);
                        inOut.setPunchTime(date);
                    } else {
                        inOut.setInOutType(InOutType.MORNING_IN);
                        inOut.setPunchTypeTime(punchTime);
                        inOut.setPunchTime(date);
                    }
                    logger.debug("Prepared OUT record for employee: {}, date: {}, time: {}",
                            employeeID, logDate, punchTime);
                    break;

                default:
                    logger.error("Invalid inout value: {} for employee: {}", inout, employeeID);
                    throw new IllegalArgumentException("Invalid inout value. Expected 'IN' or 'OUT', got: " + inout);
            }

            List<InOutEntity> existingEntries = inOutRepository.findByEmployeeIdAndDateAndPunchTime(
                    inOut.getEmployeeId(),
                    inOut.getDate(),
                    inOut.getPunchTime());

            boolean shouldSave = true;

            // If there are existing entries, check if this exact record already exists
            if (existingEntries != null && !existingEntries.isEmpty()) {
                for (InOutEntity existing : existingEntries) {
                    if (existing.equals(inOut)) {
                        shouldSave = false;
                        logger.debug("Record already exists for employee: {}, skipping save", employeeID);
                        break;
                    }
                }
            }

            if (shouldSave) {
                inOut.setCreatedDate(new Date());
                inOut.setUpdatedDate(new Date());

                InOutEntity savedInOut = inOutRepository.save(inOut);
                logger.debug("Successfully saved InOut record with ID: {} for employee: {}",
                        savedInOut.getId(), employeeID);
            }

        } catch (ParseException e) {
            logger.error("Failed to parse date while saving InOut record for employee {}: {}",
                    employeeID, logDate, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while saving InOut record for employee {}: {}",
                    employeeID, e.getMessage(), e);
            throw new RuntimeException("Failed to save InOut record", e);
        }
    }
}