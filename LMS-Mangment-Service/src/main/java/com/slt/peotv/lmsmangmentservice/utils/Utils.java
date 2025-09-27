package com.slt.peotv.lmsmangmentservice.utils;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.LeaveStatus;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private final Random RANDOM = new SecureRandom();
    private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final String ALPHANUMERIC_UPPERCASE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String NUMERIC = "0123456789";

    private static final AtomicLong counter = new AtomicLong(0);

    @Autowired
    private InOutRepo inOutRepo;
    @Autowired
    private Helper helper;

    private final String instanceId;

    public Utils() {
        this.instanceId = Long.toHexString(System.currentTimeMillis() % 10000).toUpperCase();
    }

    public String generateUserId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateAddressId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateUniqueId(int length, IdType type) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        if (length <= 6) {
            return generateShortUniqueId(length, type);
        }

        return generateLongUniqueId(length, type);
    }

    public String generateUniqueId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateUUIDBasedId(int length) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (length <= uuid.length()) {
            return uuid.substring(0, length).toUpperCase();
        } else {
            return (uuid + generateRandomString(length - uuid.length(), ALPHANUMERIC_UPPERCASE)).toUpperCase();
        }
    }

    public String generateTimestampBasedId(int length) {
        long timestamp = System.currentTimeMillis();
        long nanos = System.nanoTime() % 1000000;
        long counterVal = counter.incrementAndGet() % 1000;

        String base = Long.toString(timestamp, 36).toUpperCase() +
                Long.toString(nanos, 36).toUpperCase() +
                Long.toString(counterVal, 36).toUpperCase();

        if (base.length() >= length) {
            return base.substring(0, length);
        } else {
            return base + generateRandomString(length - base.length(), ALPHANUMERIC_UPPERCASE);
        }
    }

    private String generateShortUniqueId(int length, IdType type) {
        long timestamp = System.currentTimeMillis() % 100000;
        long counterVal = counter.incrementAndGet() % 1000;

        String charset = getCharsetByType(type);
        String base = Long.toString(timestamp, charset.length()) +
                Long.toString(counterVal, charset.length()) +
                instanceId;

        StringBuilder result = new StringBuilder();
        for (char c : base.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = Character.getNumericValue(c);
                if (digit < charset.length()) {
                    result.append(charset.charAt(digit));
                } else {
                    result.append(charset.charAt(digit % charset.length()));
                }
            } else {
                result.append(c);
            }
        }

        String uniqueBase = result.toString();
        if (uniqueBase.length() >= length) {
            return uniqueBase.substring(0, length);
        } else {
            return uniqueBase + generateRandomString(length - uniqueBase.length(), charset);
        }
    }

    private String generateLongUniqueId(int length, IdType type) {
        long timestamp = System.currentTimeMillis();
        long counterVal = counter.incrementAndGet();
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);

        String charset = getCharsetByType(type);

        int prefixLength = Math.max(3, length / 3);
        String prefix = Long.toString(timestamp, 36).toUpperCase() +
                Integer.toString(randomPart, 36).toUpperCase() +
                Long.toString(counterVal % 1000, 36).toUpperCase();

        if (prefix.length() > prefixLength) {
            prefix = prefix.substring(0, prefixLength);
        }

        int remainingLength = length - prefix.length();
        String suffix = remainingLength > 0 ?
                generateRandomString(remainingLength, charset) : "";

        return (prefix + suffix).substring(0, length);
    }

    private String generateRandomString(int length) {
        return generateRandomString(length, ALPHABET);
    }

    private String generateRandomString(int length, String charset) {
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }

        return new String(returnValue);
    }

    private String getCharsetByType(IdType type) {
        switch (type) {
            case ALPHANUMERIC:
                return ALPHABET;
            case ALPHANUMERIC_UPPERCASE:
                return ALPHANUMERIC_UPPERCASE;
            case NUMERIC:
                return NUMERIC;
            default:
                return ALPHABET;
        }
    }

    public void handleAttendanceTypeAndIssues(InOutEntity inPunch, InOutEntity outPunch, AttendanceEntity attendance,
                                               Boolean swap, Boolean fullday, Boolean half_day,
                                               Boolean unAuthorized, Boolean unSuccessful, Boolean absent, String employeeId) {

        if (attendance == null) {
            logger.error("Null attendance provided to handleAttendanceTypeAndIssues");
            return;
        }

        try {
            // Handle special flags first (highest priority)
            if (Boolean.TRUE.equals(absent)) {
                attendance.setAttendanceType(AttendanceType.ABSENT);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setHasIssues(true);
                attendance.setIsUnauthorized(false);
                attendance.setIsUnSuccessful(false);
                attendance.setIsLate(false);
                attendance.setLeaveStatus(null);
                attendance.setIssueDescription("ABSENT - NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE DUE DATE.");
                logger.info("Employee {} marked as ABSENT", employeeId);
                return;
            }

            if (Boolean.TRUE.equals(unAuthorized)) {
                // Unauthorized means missing IN or OUT punch or sequence error
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setIsUnSuccessful(false);

                if (Boolean.TRUE.equals(swap)) {
                    attendance.setIssueDescription("UNAUTHORIZED - WRONG PUNCH SEQUENCE (OUT BEFORE IN). RESOLVE BEFORE DUE DATE.");
                } else if (inPunch == null && outPunch != null) {
                    attendance.setIssueDescription("UNAUTHORIZED - MISSING IN PUNCH, ONLY OUT PUNCH FOUND. RESOLVE BEFORE DUE DATE.");
                } else if (inPunch != null && outPunch == null) {
                    attendance.setIssueDescription("UNAUTHORIZED - MISSING OUT PUNCH, ONLY IN PUNCH FOUND. RESOLVE BEFORE DUE DATE.");
                } else {
                    attendance.setIssueDescription("UNAUTHORIZED - PUNCH DATA ERROR. RESOLVE BEFORE DUE DATE.");
                }
                logger.warn("Employee {} marked as UNAUTHORIZED: {}", employeeId, attendance.getIssueDescription());
                return;
            }

            // For valid punch scenarios, we need both IN and OUT or explicit flags
            if (inPunch == null || inPunch.getPunchTypeTime() == null) {
                logger.warn("No valid IN punch for employee: {}", employeeId);
                // Treat as unauthorized if not explicitly handled above
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setIssueDescription("MISSING OR INVALID IN PUNCH DATA");
                return;
            }

            LocalTime arrivalTime = inPunch.getPunchTypeTime().toLocalTime();
            LocalTime departureTime = null;
            Duration workDuration = null;
            long workHours = 0;
            boolean hasValidOutPunch = (outPunch != null && outPunch.getPunchTypeTime() != null);

            if (hasValidOutPunch) {
                departureTime = outPunch.getPunchTypeTime().toLocalTime();
                workDuration = Duration.between(arrivalTime, departureTime);
                workHours = workDuration.toHours();
            }

            // Company policy time thresholds
            LocalTime standardArrival = LocalTime.of(8, 30);
            LocalTime lateThreshold = LocalTime.of(9, 0);
            LocalTime veryLateThreshold = LocalTime.of(12, 0);
            LocalTime criticalThreshold = LocalTime.of(13, 0);
            LocalTime standardDeparture = LocalTime.of(17, 0);
            LocalTime tenAm = LocalTime.of(10, 0);

            logger.debug("Processing employee {}: Arrival={}, Departure={}, WorkHours={}, Flags: fullday={}, half_day={}, unSuccessful={}",
                    employeeId, arrivalTime, departureTime, workHours, fullday, half_day, unSuccessful);

            // EXPLICIT FLAG HANDLING - Override time-based logic when flags are set
            if (Boolean.TRUE.equals(half_day)) {
                // Explicitly marked as half day
                attendance.setAttendanceType(AttendanceType.HALF_DAY);
                attendance.setLeaveStatus(null);
                attendance.setIsLate(arrivalTime.isAfter(standardArrival));

                if (hasValidOutPunch && workHours >= 4) {
                    attendance.setHasIssues(false);
                    attendance.setIssueDescription(String.format("APPROVED HALF DAY - WORKED %d HOURS", workHours));
                    attendance.setDueDateForUA(null);
                } else if (hasValidOutPunch) {
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("HALF DAY BUT INSUFFICIENT HOURS (%d) - VERIFY ATTENDANCE", workHours));
                    attendance.setIsUnauthorized(true);
                } else {
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription("HALF DAY APPROVED BUT NO OUT PUNCH - VERIFY ATTENDANCE");
                    attendance.setIsUnauthorized(true);
                    attendance.setDueDateForUA(helper.getDueDate());
                }

            } else if (Boolean.TRUE.equals(fullday)) {
                // Explicitly marked as full day
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                attendance.setIsLate(arrivalTime.isAfter(standardArrival));

                if (hasValidOutPunch && workHours >= 8) {
                    // Full day with adequate hours
                    attendance.setLeaveStatus(null);
                    attendance.setHasIssues(false);
                    attendance.setDueDateForUA(null);
                    if (arrivalTime.isAfter(standardArrival)) {
                        attendance.setIssueDescription(String.format("FULL DAY COMPLETED DESPITE LATE ARRIVAL (%s)", arrivalTime.toString()));
                    } else {
                        attendance.setIssueDescription(null);
                    }
                } else if (hasValidOutPunch && workHours >= 6) {
                    // Full day but short hours
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("FULL DAY BUT ONLY WORKED %d HOURS - SHORT LEAVE APPLIED", workHours));
                    attendance.setIsUnauthorized(true);
                } else if (hasValidOutPunch) {
                    // Full day but very short hours
                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("FULL DAY MARKED BUT ONLY WORKED %d HOURS - FULL LEAVE", workHours));
                    attendance.setIsUnauthorized(true);
                } else {
                    // Full day but no out punch
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription("FULL DAY MARKED BUT NO OUT PUNCH - VERIFY DEPARTURE TIME");
                    attendance.setIsUnauthorized(true);
                }

            } else {
                // NO EXPLICIT FLAGS - Use time-based logic

                if (!hasValidOutPunch) {
                    // Only IN punch, no OUT punch - Unauthorized
                    attendance.setHasIssues(true);
                    attendance.setIsUnauthorized(true);
                    attendance.setIsLate(arrivalTime.isAfter(standardArrival));
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("MISSING OUT PUNCH - ARRIVED AT %s BUT NO DEPARTURE RECORD", arrivalTime.toString()));

                } else {
                    // Both IN and OUT punches available - Apply comprehensive logic

                    if (arrivalTime.isAfter(criticalThreshold)) {
                        // Critical late arrival (after 1 PM)
                        if (workHours >= 4 && departureTime.isAfter(standardDeparture)) {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("CRITICAL LATE ARRIVAL (%s) WITH OVERTIME COMPENSATION", arrivalTime.toString()));
                            attendance.setHasIssues(true);
                        } else {
                            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                            attendance.setIssueDescription(String.format("CRITICAL LATE ARRIVAL (%s) - INSUFFICIENT COMPENSATION", arrivalTime.toString()));
                            attendance.setHasIssues(true);
                            attendance.setIsUnauthorized(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(veryLateThreshold)) {
                        // Very late arrival (after noon)
                        if (workHours >= 4) {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setIssueDescription(String.format("VERY LATE ARRIVAL BUT WORKED %d HOURS - HALF DAY", workHours));
                            attendance.setHasIssues(false);
                        } else {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("VERY LATE ARRIVAL - INSUFFICIENT HOURS (%d)", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsUnauthorized(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(lateThreshold)) {
                        // Moderate late arrival (after 9.00 AM)
                        if (workHours >= 6 && departureTime.isAfter(standardDeparture)) {
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("LATE ARRIVAL PARTIALLY COMPENSATED - WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                        } else if (arrivalTime.isBefore(tenAm)) {
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("MODERATE LATE ARRIVAL - INSUFFICIENT COMPENSATION", workHours));
                            attendance.setHasIssues(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(standardArrival)) {
                        // Minor late arrival (after 8:30 AM)
                        if (departureTime.isAfter(standardDeparture)) {
                            long lateMinutes = Duration.between(standardArrival, arrivalTime).toMinutes();
                            LocalTime requiredDeparture = standardDeparture.plusMinutes(lateMinutes);

                            if (departureTime.isAfter(requiredDeparture) || departureTime.equals(requiredDeparture)) {
                                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                                attendance.setLeaveStatus(null);
                                attendance.setIssueDescription("LATE ARRIVAL FULLY COMPENSATED WITH OVERTIME");
                                attendance.setHasIssues(false);
                                attendance.setDueDateForUA(null);
                                attendance.setIsLateCovered(true);

                            } else {
                                attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                                attendance.setIssueDescription("LATE ARRIVAL PARTIALLY COMPENSATED");
                                attendance.setHasIssues(true);
                                attendance.setIsUnSuccessful(true);
                            }
                        } else {
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription("LATE ARRIVAL WITHOUT COMPENSATION");
                            attendance.setHasIssues(true);
                            attendance.setDueDateForUA(helper.getDueDate());
                            attendance.setIsUnauthorized(true);
                        }
                        attendance.setIsLate(true);

                    } else {
                        // On-time or early arrival
                        if (departureTime.isAfter(standardDeparture) || departureTime.equals(standardDeparture)) {
                            // Perfect attendance
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setHasIssues(false);
                            attendance.setIsLate(false);
                            attendance.setDueDateForUA(null);
                            attendance.setIssueDescription(null);
                        } else if (workHours >= 6) {
                            // Early departure but adequate hours
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("EARLY DEPARTURE BUT WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsLate(false);
                        } else if (workHours >= 4) {
                            // Half day scenario
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setIssueDescription(String.format("HALF DAY - WORKED %d HOURS", workHours));
                            attendance.setHasIssues(false);
                            attendance.setIsLate(false);
                        } else {
                            // Very early departure
                            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                            attendance.setIssueDescription(String.format("VERY EARLY DEPARTURE - ONLY WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsLate(false);
                            attendance.setIsUnauthorized(true);
                            attendance.setDueDateForUA(helper.getDueDate());
                        }
                    }
                }
            }

            // Apply unsuccessful flag if explicitly set
            if (Boolean.TRUE.equals(unSuccessful)) {
                attendance.setIsUnSuccessful(true);
                attendance.setHasIssues(true);
                if (attendance.getIssueDescription() == null || attendance.getIssueDescription().isEmpty()) {
                    attendance.setIssueDescription("MARKED AS UNSUCCESSFUL DUE TO ATTENDANCE ISSUES");
                } else {
                    attendance.setIssueDescription(attendance.getIssueDescription() + " - UNSUCCESSFUL");
                }
            }

            // Set default values for unset fields
            if (attendance.getIsUnauthorized() == null) {
                attendance.setIsUnauthorized(false);
            }
            if (attendance.getIsUnSuccessful() == null) {
                attendance.setIsUnSuccessful(attendance.getHasIssues() != null ? attendance.getHasIssues() : false);
            }
            if (attendance.getIsLate() == null) {
                attendance.setIsLate(false);
            }

            logger.info("Employee {}: Final attendance - Type: {}, Leave: {}, Late: {}, Issues: {}, Unauthorized: {}, Description: {}",
                    employeeId, attendance.getAttendanceType(), attendance.getLeaveStatus(),
                    attendance.getIsLate(), attendance.getHasIssues(), attendance.getIsUnauthorized(),
                    attendance.getIssueDescription());

        } catch (Exception e) {
            logger.error("Error handling attendance type and issues for employee: {}", employeeId, e);
            // Set safe defaults in case of error
            attendance.setHasIssues(true);
            attendance.setIssueDescription("ERROR IN ATTENDANCE PROCESSING: " + e.getMessage());
            attendance.setIsUnauthorized(true);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setAttendanceType(AttendanceType.NONE);
            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
        }
    }

    public void handleLeaveStatus(AttendanceEntity attendance, Boolean leaveSuccess, Boolean leaveReq, Boolean isFullLeave) {
        if (attendance == null) {
            logger.error("Null attendance provided to handleLeaveStatus");
            return;
        }

        try {
            if (Boolean.TRUE.equals(isFullLeave)) {
                attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
            } else if (Boolean.TRUE.equals(leaveSuccess)) {
                attendance.setLeaveStatus(LeaveStatus.LEAVE_APPROVED);
            } else if (Boolean.TRUE.equals(leaveReq)) {
                attendance.setLeaveStatus(LeaveStatus.LEAVE_REQUESTED);
            }
        } catch (Exception e) {
            logger.error("Error handling leave status for attendance: {}", attendance.getId(), e);
        }
    }

    public void updateInOutRelationships(InOutEntity moa, InOutEntity eve, AttendanceEntity savedAttendance) {
        if (savedAttendance == null) {
            logger.error("Null attendance provided to updateInOutRelationships");
            return;
        }

        try {

            if (moa != null && moa.getId() != null) {
                Optional<InOutEntity> moaEntity = inOutRepo.findById(moa.getId());
                if (moaEntity.isPresent()) {
                    InOutEntity managedMoa = moaEntity.get();
                    managedMoa.setAttendance(savedAttendance);
                    inOutRepo.save(managedMoa);
                } else {
                    logger.warn("Morning inout entity not found for ID: {}", moa.getId());
                }
            }

            if (eve != null && eve.getId() != null) {
                Optional<InOutEntity> eveEntity = inOutRepo.findById(eve.getId());
                if (eveEntity.isPresent()) {
                    InOutEntity managedEve = eveEntity.get();
                    managedEve.setAttendance(savedAttendance);
                    inOutRepo.save(managedEve);
                } else {
                    logger.warn("Evening inout entity not found for ID: {}", eve.getId());
                }
            }

            logger.info("InOut relationships established for attendance: {}", savedAttendance.getId());

        } catch (Exception e) {
            logger.error("Error establishing InOut relationships for attendance: {}", savedAttendance.getId(), e);
        }
    }


    public enum IdType {
        ALPHANUMERIC,
        ALPHANUMERIC_UPPERCASE,
        NUMERIC
    }

}