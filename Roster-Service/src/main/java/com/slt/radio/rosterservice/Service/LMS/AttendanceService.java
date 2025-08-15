package com.slt.radio.rosterservice.Service.LMS;

import com.slt.radio.rosterservice.Model.Enum.AttendanceType;
import com.slt.radio.rosterservice.Model.Enum.InOutType;
import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import com.slt.radio.rosterservice.Model.One.Employeee.EmployeeArchive;
import com.slt.radio.rosterservice.Model.One.LMS.*;
import com.slt.radio.rosterservice.Model.One.Roster;
import com.slt.radio.rosterservice.Model.One.Shift.ShiftAssignment;
import com.slt.radio.rosterservice.Model.One.Shift.ShiftRoster;
import com.slt.radio.rosterservice.Model.One.Teamm.Team;
import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Repo.*;
import com.slt.radio.rosterservice.Utils.Helper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.slt.radio.rosterservice.messaging.MessageProducerService;
import org.springframework.beans.factory.annotation.Value;
import com.slt.radio.rosterservice.Model.Enum.LeaveStatus;

import java.time.format.DateTimeParseException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.temporal.TemporalAdjusters;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import com.slt.radio.rosterservice.Model.Enum.RosterType;
import com.slt.radio.rosterservice.messaging.AttendanceJSM;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final LocalTime NOON = LocalTime.NOON;

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_ =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));

    private static final ThreadLocal<SimpleDateFormat> ALT_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    private final ReentrantLock processLock = new ReentrantLock();

    private final InOutRepository inOutRepository;
    private final AttendanceRepository attendanceRepository;
    private final RosterAttendanceRepository rosterAttendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final ShiftRosterRepository shiftRosterRepository;
    private final RosterRepository rosterRepository;
    private final EmployeeArchiveRepository employeeArchiveRepository;
    private final DutyRosterRepository dutyRosterRepository;
    private final Helper helper;
    private final MessageProducerService messageProducerService;
    private final LocalTime fullDayLeaveTime = LocalTime.of(13, 0);

    @Value("${ROSTER_BEYOND_TwentyFour:false}")
    private boolean roster_beyond;

    @Transactional
    public void processAbsentAttendance() {
        List<EmployeeArchive> roasterAbsent = employeeArchiveRepository.findByRoaster(true);
        roasterAbsent.forEach(employee -> {

            if(attendanceRepository.existsByEmployeeIdAndDate(employee.getEmployeeId(), helper.getYesterdayDate()) ||
                    attendanceRepository.existsByEmployeeIdAndArrivalDate(employee.getEmployeeId(), helper.getYesterdayDate())) return;

            Attendance attendance = new Attendance();
            attendance.setPublicId(UUID.randomUUID().toString());
            attendance.setIsManual(true);
            attendance.setRosterType(RosterType.NONE);
            attendance.setAttendanceType(AttendanceType.NONE);
            attendance.setEmployeeId(employee.getEmployeeId());
            attendance.setArrivalDate(helper.getYesterdayDate());
            attendance.setDate(helper.getYesterdayDate());
            Attendance save = attendanceRepository.save(attendance);

            if(!roster_beyond){
                messageProducerService.sendMessage("roster.queue", convertToAttendanceJSM(save));
            }
        });
    }

    @Transactional
    public void processDutyAttendances() {
        LocalDate today = LocalDate.now().minusDays(1);
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        DutyRoster mainDuty = null;
        DutyRoster duty_ = dutyRosterRepository.findByWeekStartingDate(startOfWeek).orElse(null);

        if (duty_ != null) {
            List<DutyRoster> allActiveRosters = dutyRosterRepository.findByIsActive(true);
            allActiveRosters.forEach(roster -> {
                roster.setActive(false);
                dutyRosterRepository.save(roster);
            });

            // Then activate the target roster
            duty_.setActive(true);
            mainDuty = dutyRosterRepository.save(duty_);
        } else {
            // If no roster found for the week, try to find any active roster
            Optional<DutyRoster> activeRosterOpt = dutyRosterRepository.findByIsActive(true)
                    .stream()
                    .findFirst(); // Get the first one if multiple exist
            mainDuty = activeRosterOpt.orElse(null);
        }

        if (mainDuty == null) return;

        Date processDate = helper.getYesterdayDate();
        List<Attendance> attendancesToSave = new CopyOnWriteArrayList<>();

        mainDuty.getDailyDuties().forEach(dailyDuty -> {
            if (!dailyDuty.getDayOfWeek().equals(today.getDayOfWeek())) {
                return;
            }

            dailyDuty.getTimeSlots().forEach(timeSlot -> {
                timeSlot.getAssignedEmployees().parallelStream().forEach(emId -> {
                    try {
                        String cleanEmId = cleanEmployeeId(emId);
                        if (cleanEmId.isEmpty()) {
                            log.warn("Invalid employee ID format: {}", emId);
                            return;
                        }

                        Optional<EmployeeArchive> employeeOpt = employeeArchiveRepository.findByEmployeeId(cleanEmId);
                        if (employeeOpt.isEmpty() || !employeeOpt.get().getRoaster()) {
                            return;
                        }

                        EmployeeArchive employee = employeeOpt.get();

                        List<InOut> punches = inOutRepository.findByEmployeeIdAndPunchTime(employee.getSltId(), processDate);

                        Optional<InOut> earliestPunchIn = punches.stream()
                                .filter(inOut -> inOut.getInOutValue() == 1)
                                .min(Comparator.comparing(InOut::getPunchTypeTime));

                        Optional<InOut> latestPunchOut = punches.stream()
                                .filter(inOut -> inOut.getInOutValue() == 0)
                                .max(Comparator.comparing(InOut::getPunchTypeTime));

                        Attendance attendance = new Attendance();
                        attendance.setIsManual(true);
                        attendance.setRosterType(RosterType.CHARANA_TV);
                        attendance.setPublicId(UUID.randomUUID().toString());
                        attendance.setEmployeeId(cleanEmId);
                        attendance.setDate(processDate);
                        attendance.setArrivalDate(processDate);

                        if (earliestPunchIn.isEmpty()) {
                            log.debug("No morning punch-in for employee: {}", cleanEmId);
                            attendance.setAttendanceType(AttendanceType.ABSENT);
                            attendance.setHasIssues(true);

                            if (latestPunchOut.isPresent()) {
                                InOut out = latestPunchOut.get();
                                if (fullDayLeaveTime.isAfter(out.getPunchTypeTime())) {
                                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                                }
                                attendance.setIsUnauthorized(true);
                                attendance.setLeftTime(out.getPunchTypeTime());
                                attendance.setAttendanceType(AttendanceType.NONE);
                            } else {
                                attendance.setIssueDescription(
                                        "GOING ABSENT DUE TO NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE THE DUE DATE.");
                            }

                            attendancesToSave.add(attendance);
                            return;
                        }

                        InOut in = earliestPunchIn.get();
                        InOut out = latestPunchOut.orElse(null);

                        LocalTime timeIn = in.getPunchTypeTime();
                        LocalTime timeOut = out != null ? out.getPunchTypeTime() : null;

                        LocalTime startTime = timeSlot.getStartTime();
                        LocalTime endTime = timeSlot.getEndTime();

                        attendance.setArrivalDate(in.getPunchTime());
                        attendance.setArrivalTime(timeIn);
                        attendance.setTerminalId(in.getTerminalId());
                        if (out != null) {
                            attendance.setLeftTime(timeOut);
                            if (!in.getTerminalId().equals(out.getTerminalId())) {
                                attendance.setTerminalId(in.getTerminalId() + " - " + out.getTerminalId());
                            }
                        }

                        if (startTime.isBefore(timeIn)) {
                            long lateMinutes = Duration.between(startTime, timeIn).toMinutes();
                            if (lateMinutes > 4 * 60) {
                                attendance.setAttendanceType(AttendanceType.HALF_DAY);
                                attendance.setIsLate(true);
                                attendance.setHasIssues(true);
                            } else {
                                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                                attendance.setIsLate(true);
                                attendance.setHasIssues(true);
                            }
                        } else {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                        }

                        if ((in == null && out != null) || (in != null && out == null)) {
                            attendance.setIsUnauthorized(true);
                            attendance.setHasIssues(true);
                            attendance.setIssueDescription(
                                    "GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
                        }

                        if (!attendance.getIsLate() && timeIn.isAfter(startTime) &&
                                timeOut != null && timeOut.isBefore(endTime)) {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                        }

                        attendancesToSave.add(attendance);

                    } catch (Exception e) {
                        log.error("Error processing employee {}: {}", emId, e.getMessage(), e);
                    }
                });
            });
        });

        // Save only unique, non-duplicate attendances
        if (!attendancesToSave.isEmpty()) {
            List<Attendance> safeList = new ArrayList<>(attendancesToSave);

            Set<String> existingEmployeeIds = attendanceRepository.findExistingAttendances(
                            safeList.stream().map(Attendance::getEmployeeId).toList(),
                            helper.stripTimeFromDate(new Date())
                    ).stream()
                    .map(Attendance::getEmployeeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Attendance> uniqueAttendances = safeList.stream()
                    .filter(a -> !existingEmployeeIds.contains(a.getEmployeeId()))
                    .filter(a -> !helper.isDuplicateAttendance(a))
                    .toList();

            if (!uniqueAttendances.isEmpty()) {
                List<Attendance> saved = attendanceRepository.saveAll(uniqueAttendances);
                saved.forEach(attendance -> {
                    if (!roster_beyond) {
                        messageProducerService.sendMessage("roster.queue", convertToAttendanceJSM(attendance));
                    }
                });
            }
        }
    }

    private String cleanEmployeeId(String rawId) {
        if (rawId == null) return "";

        String cleaned = rawId.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-zA-Z0-9]", "");

        return cleaned.toUpperCase();
    }


    public Optional<ShiftRoster> getAttendance(int year, String month) {
        return shiftRosterRepository.findByMonthAndYear(month, year);
    }

    @Transactional
    public void processAccessLogs(List<AccessLog> accessLogs) {
        if (accessLogs == null || accessLogs.isEmpty()) {
            return;
        }

        ConcurrentHashMap<String, List<AccessLog>> employeeLogsMap =
                (ConcurrentHashMap<String, List<AccessLog>>) accessLogs.parallelStream()
                        .collect(Collectors.groupingByConcurrent(AccessLog::getEmployeeId));

        employeeLogsMap.forEach((employeeId, logs) -> {
            ConcurrentHashMap<String, List<AccessLog>> dateLogsMap =
                    (ConcurrentHashMap<String, List<AccessLog>>) logs.parallelStream()
                            .collect(Collectors.groupingByConcurrent(AccessLog::getLogDate));

            dateLogsMap.forEach((date, dailyLogs) -> {
                try {
                    createInOutFromLogsV1(employeeId, dailyLogs);
                } catch (ParseException e) {
                    log.error("Error parsing date/time for employee: {} on date: {}", employeeId, date, e);
                }
            });
        });
    }

    private LocalTime parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            log.warn("Empty or null time string provided");
            return null;
        }

        try {
            String cleanTime = timeStr.trim();

            if (cleanTime.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                String[] parts = cleanTime.split(":");
                cleanTime = parts[0] + ":" + parts[1];
            }

            return LocalTime.parse(cleanTime, TIME_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse time string '{}': {}", timeStr, e.getMessage());
            return null;
        }
    }

    private int extractHourSafely(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            log.warn("Empty or null time string for hour extraction");
            return -1;
        }

        try {
            String cleanTime = timeStr.trim();
            String[] parts = cleanTime.split(":");

            if (parts.length < 2) {
                log.warn("Invalid time format '{}' - expected HH:mm or HH:mm:ss", timeStr);
                return -1;
            }

            String hourStr = parts[0].trim();
            if (hourStr.isEmpty()) {
                log.warn("Empty hour part in time string '{}'", timeStr);
                return -1;
            }

            return Integer.parseInt(hourStr);
        } catch (NumberFormatException e) {
            log.error("Failed to parse hour from time string '{}': {}", timeStr, e.getMessage());
            return -1;
        }
    }

    private boolean isValidTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }

        String cleanTime = timeStr.trim();
        return cleanTime.matches("^\\d{1,2}:\\d{2}(:\\d{2})?$");
    }

    private InOutType determineInOutType(String inOutStatus, int hour) {
        boolean isMorning = hour < 12;

        if ("IN".equals(inOutStatus)) {
            return isMorning ? InOutType.MORNING_IN : InOutType.EVENING_IN;
        } else if ("OUT".equals(inOutStatus)) {
            return isMorning ? InOutType.MORNING_OUT : InOutType.EVENING_OUT;
        }

        return InOutType.SINGLE_PUNCH;
    }



    @Transactional
    public void createInOutFromLogsV1(String employeeId, List<AccessLog> logs) throws ParseException {
        if (logs == null || logs.isEmpty()) {
            log.warn("No logs provided for employee: {}", employeeId);
            return;
        }

        List<AccessLog> sortedLogs = new ArrayList<>(logs);
        sortedLogs.sort(Comparator.comparing(AccessLog::getLogTime));

        for (AccessLog log : sortedLogs) {
            try {
                String timeStr = log.getLogTime().trim();

                if (!isValidTimeFormat(timeStr)) {
                    logger.warn("Invalid time format in log for employee: {} - time: {}", employeeId, timeStr);
                    continue;
                }

                int hour = extractHourSafely(timeStr);
                if (hour == -1) {
                    logger.warn("Skipping log with invalid time format for employee: {} - time: {}", employeeId, timeStr);
                    continue;
                }

                InOutType inOutType = determineInOutType(log.getInOut(), hour);
                Date logDate = ALT_DATE_FORMAT.get().parse(log.getLogDate());

                LocalTime parsedTime = parseTimeString(timeStr);
                boolean isMorning = parsedTime.isBefore(NOON);


                String inValue = log.getInOut().trim();
                assert parsedTime != null;


                InOut inOut = InOut.builder()
                        .employeeId(employeeId)
                        .date(helper.stripTimeFromDate(helper.getYesterdayDate()))
                        .punchTime(helper.stripTimeFromDate(logDate))
                        .punchTypeTime(parsedTime)
                        .terminalId(log.getTerminalId())
                        .inOutValue("IN".equals(log.getInOut().trim()) ? 1 : 0)
                        .inOutType(inOutType)
                        .etlRunTime(new Date())
                        .build();


                switch (inValue) {
                    case "IN":
                        inOut.setInOutValue(1);
                        inOut.setInOutType(isMorning ? InOutType.MORNING_IN : InOutType.EVENING_IN);
                        break;
                    case "OUT":
                        inOut.setInOutValue(0);
                        inOut.setInOutType(isMorning ? InOutType.MORNING_OUT : InOutType.EVENING_OUT);
                        break;
                    default:
                        logger.error("Invalid inout value for employee {}. Value: {}", log.getEmployeeId(), inOut);
                        return;
                }

                boolean isDuplicate = helper.checkForDuplicateInOut(inOut);

                if (!isDuplicate) {
                    InOut saved = inOutRepository.save(inOut);
                    logger.info("Saved InOut record: {}", saved);
                } else {
                    logger.debug("Skipped duplicate InOut record for employee: {}", employeeId);
                }
            } catch (Exception e) {
                logger.error("Error processing log for employee: {} - log: {} - error: {}",
                        employeeId, log, e.getMessage());
            }
        }
    }


    @Transactional
    public RosterAttendance processAttendanceForDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.get().parse(dateStr);
            Date processDate = helper.stripTimeFromDate(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(processDate);
            int monthNumber = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            Month month = Month.of(monthNumber);
            String monthDisplayName = month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);

            Optional<ShiftRoster> shiftRosterOpt = shiftRosterRepository.findByMonthAndYear(monthDisplayName, year);
            if (shiftRosterOpt.isEmpty()) {
                log.error("No shift roster found for month: {} and year: {}", month, year);
                return null;
            }

            ShiftRoster shiftRoster = shiftRosterOpt.get();
            String dayPart = dateStr.split("/")[0];
            dayPart = dayPart.replaceFirst("^0+(?!$)", "");

            if (!shiftRoster.getDates().contains(dayPart)) {
                log.error("Date: {} not found in shift roster", dateStr);
                return null;
            }

            ConcurrentHashMap<String, TeamAttendanceSummary> teamSummaries = new ConcurrentHashMap<>();
            List<EmployeeAttendanceDetail> employeeDetails = Collections.synchronizedList(new ArrayList<>());

            processShifts(dateStr, shiftRoster.getDutyTurn(), teamSummaries, employeeDetails, false);

            if (shiftRoster.getDutyTurn().containsKey("ROT")) {
                processShifts(dateStr, Collections.singletonMap("ROT", shiftRoster.getDutyTurn().get("ROT")),
                        teamSummaries, employeeDetails, true);
            }

            if(rosterAttendanceRepository.findByDate(dateStr).isEmpty()) {
                RosterAttendance rosterAttendance = RosterAttendance.builder()
                        .date(dateStr)
                        .month(monthNumber)
                        .year(year)
                        .teamAttendanceSummary(teamSummaries)
                        .employeeAttendanceDetails(new ArrayList<>(employeeDetails))
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                return rosterAttendanceRepository.save(rosterAttendance);
            }
            return null;
        } catch (Exception e) {
            log.error("Error processing attendance for date: {}", dateStr, e);
            return null;
        }
    }

    private void processShifts(String dateStr, Map<String, List<ShiftAssignment>> shifts,
                               ConcurrentHashMap<String, TeamAttendanceSummary> teamSummaries,
                               List<EmployeeAttendanceDetail> employeeDetails,
                               boolean isRotationShift) {

        shifts.forEach((shiftTime, assignments) -> {
            assignments.forEach(assignment -> {
                String dayPart = dateStr.split("/")[0];
                dayPart = dayPart.replaceFirst("^0+(?!$)", "");

                if (!dayPart.equals(assignment.getDate())) return;

                String teamNameOrId = assignment.getTeam();
                boolean isTeamRotation = teamNameOrId.contains("ROT");

                if (isRotationShift != isTeamRotation) return;

                String teamId = teamNameOrId.replace(" ROT", "");
                Team team = teamRepository.findByShortName(teamId).orElse(null);
                if (team == null) {
                    log.error("Team not found: {}", teamId);
                    return;
                }

                List<Employee> teamEmployees = employeeRepository.findByTeamId(team.getId());
                if (teamEmployees.isEmpty()) {
                    log.warn("No employees for team: {}", team.getName());
                    return;
                }

                List<Attendance> attendances = Collections.synchronizedList(new ArrayList<>());

                teamEmployees.stream()
                        .map(employee -> processEmployee(employee, dateStr, shiftTime, team, isRotationShift, employeeDetails))
                        .filter(Objects::nonNull)
                        .filter(attendance -> !helper.isDuplicateAttendance(attendance))
                        .forEach(attendances::add);

                if (!attendances.isEmpty()) {
                    List<Attendance> attendances_save = attendanceRepository.saveAll(attendances);
                    attendances_save.forEach(attendance -> {
                        if(!roster_beyond){
                            messageProducerService.sendMessage("roster.queue", convertToAttendanceJSM(attendance));
                        }

                    });
                }

                TeamAttendanceSummary summary = createTeamSummary(team, shiftTime, isRotationShift, attendances, teamEmployees.size());
                teamSummaries.put(team.getId(), summary);
            });
        });
    }

    public static AttendanceJSM convertToAttendanceJSM(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceJSM attendanceJSM = new AttendanceJSM();
        attendanceJSM.setId(attendance.getId());
        attendanceJSM.setPublicId(attendance.getPublicId());
        attendanceJSM.setDate(attendance.getDate());
        attendanceJSM.setArrivalDate(attendance.getArrivalDate());
        attendanceJSM.setArrivalTime(attendance.getArrivalTime());
        attendanceJSM.setLeftTime(attendance.getLeftTime());
        attendanceJSM.setTerminalId(attendance.getTerminalId());
        attendanceJSM.setEmployeeId(attendance.getEmployeeId());
        attendanceJSM.setTeamId(attendance.getTeamId());
        attendanceJSM.setAttendanceType(attendance.getAttendanceType());
        attendanceJSM.setRosterType(attendance.getRosterType());
        attendanceJSM.setLate(attendance.getIsLate());
        attendanceJSM.setLateCovered(attendance.getIsLateCovered());
        attendanceJSM.setUnauthorized(attendance.getIsUnauthorized());
        attendanceJSM.setUnSuccessful(attendance.getIsUnSuccessful());
        attendanceJSM.setHoliday(attendance.getIsHoliday());
        attendanceJSM.setResolved(attendance.getIsResolved());
        attendanceJSM.setHasIssues(attendance.getHasIssues());
        attendanceJSM.setManual(attendance.getIsManual());
        attendanceJSM.setIssueDescription(attendance.getIssueDescription());
        attendanceJSM.setDueDateForUA(attendance.getDueDateForUA());
        attendanceJSM.setEtlRunTime(attendance.getEtlRunTime());
        attendanceJSM.setCreatedDate(attendance.getCreatedDate());
        attendanceJSM.setUpdatedDate(attendance.getUpdatedDate());
        attendanceJSM.setActive(attendance.getIsActive());
        attendanceJSM.setViaMovement(attendance.getViaMovement());
        attendanceJSM.setViaLeave(attendance.getViaLeave());

        return attendanceJSM;
    }

    private TeamAttendanceSummary createTeamSummary(Team team, String shiftTime, boolean isRotationShift,
                                                    List<Attendance> attendances, int totalEmployees) {
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int halfDayCount = 0;

        List<Attendance> safeAttendances = new ArrayList<>(attendances);

        for (Attendance attendance : safeAttendances) {
            if (attendance.getAttendanceType() == AttendanceType.ABSENT) {
                absentCount++;
            } else if (Boolean.TRUE.equals(attendance.getIsLate())) {
                lateCount++;
                if (attendance.getAttendanceType() == AttendanceType.HALF_DAY) {
                    halfDayCount++;
                }
            } else {
                presentCount++;
            }
        }

        return TeamAttendanceSummary.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .shiftTime(shiftTime)
                .totalEmployees(totalEmployees)
                .presentEmployees(presentCount)
                .lateEmployees(lateCount)
                .absentEmployees(absentCount)
                .halfDayEmployees(halfDayCount)
                .isRotationShift(isRotationShift)
                .build();
    }

    public Map<String, InOut> getEarliestAndLatestPunch(String employeeId, Date processDate) {

        InOut earliestPunch = inOutRepository.findEarliestPunchAfterTime(
                employeeId,
                helper.stripTimeFromDate(helper.getYesterdayDate_()),
                LocalTime.of(20, 30)
        ).orElse(null);

        InOut latestPunch = inOutRepository.findLatestPunchBeforeTime(
                employeeId,
                helper.stripTimeFromDate(helper.getTomorrowDate()),
                LocalTime.of(3, 0)
        ).orElse(null);

        Map<String, InOut> result = new HashMap<>();
        result.put("earliest", earliestPunch);
        result.put("latest", latestPunch);

        return result;
    }

    private Attendance processEmployee(Employee employee, String dateStr, String shiftTime,
                                       Team team, boolean isRotationShift,
                                       List<EmployeeAttendanceDetail> employeeDetails) {
        try {
            Date processDate = helper.stripTimeFromDate(helper.getYesterdayDate());

            EmployeeArchive employeeArchive = employeeArchiveRepository.findByEmployeeId(employee.getEmployeeId()).orElse(null);
            if (employeeArchive == null) return null;
            if (!employeeArchive.getRoaster()) return null;

            /* Optional<InOut> earliestPunchIn = inOutRepository.findByEmployeeIdAndPunchTimeAndInOutType(employeeArchive.getSltId(), processDate, InOutType.MORNING_IN).
                    stream().min(Comparator.comparing(InOut::getPunchTypeTime));

            Optional<InOut> latestPunchIn = inOutRepository.findByEmployeeIdAndPunchTimeAndInOutType(
                    employeeArchive.getSltId(),
                    processDate,
                    InOutType.EVENING_OUT
            ).stream().max(Comparator.comparing(InOut::getPunchTypeTime)); */

            List<InOut> punches = inOutRepository.findByEmployeeIdAndPunchTime(employeeArchive.getSltId(), processDate);

            Optional<InOut> earliestPunchIn = punches.stream()
                    .filter(inOut -> inOut.getInOutValue() == 1)
                    .min(Comparator.comparing(InOut::getPunchTypeTime));

            Optional<InOut> latestPunchOut = punches.stream()
                    .filter(inOut -> inOut.getInOutValue() == 0)
                    .max(Comparator.comparing(InOut::getPunchTypeTime));

            InOut inOut = earliestPunchIn.orElse(null);
            InOut inOutLatest = latestPunchOut.orElse(null);

            if (inOut == null) {
                if(inOutLatest != null)
                    return createAbsentAttendance(employee, team, processDate, shiftTime, isRotationShift, employeeDetails, inOutLatest);
                else
                    return createAbsentAttendance(employee, team, processDate, shiftTime, isRotationShift, employeeDetails, null);
            }

            String[] shiftTimeParts = shiftTime.split("-");
            if (shiftTimeParts.length != 2) {
                log.error("Invalid shift time format: {}", shiftTime);
                return null;
            }

            LocalTime expectedStartTime = parseTimeString(shiftTimeParts[0].trim());
            LocalTime expectedEndTime = parseTimeString(shiftTimeParts[1].trim());
            if (expectedStartTime == null || expectedEndTime == null) {
                log.error("Could not parse expected times: {}", shiftTime);
                return null;
            }

            LocalTime actualStartTime = inOut.getPunchTypeTime();
            LocalTime actualEndTime = inOutLatest != null ? inOutLatest.getPunchTypeTime() : null;

            Map<String, InOut> punchData = getEarliestAndLatestPunch(employeeArchive.getSltId(), processDate);

            if (expectedStartTime.equals(LocalTime.MIDNIGHT)) {
                InOut firstPunch = punchData.get("earliest");
                if(firstPunch != null) inOut = firstPunch;
            }

            if (expectedEndTime.equals(LocalTime.MIDNIGHT)) {
                InOut lastPunch = punchData.get("latest");
                if(lastPunch != null) inOutLatest = lastPunch;
            }

            boolean isLate = actualStartTime.isAfter(expectedStartTime);
            long lateMinutes = Duration.between(expectedStartTime, actualStartTime).toMinutes();
            boolean isHalfDayLate = lateMinutes > (4 * 60);

            Attendance attendance = buildBaseAttendance(employee, team, processDate, shiftTime, isRotationShift);

            if((inOut == null && inOutLatest != null) || (inOut != null && inOutLatest == null)) {
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
            }

            if(isLate) {
                attendance.setIsLate(true);
                attendance.setHasIssues(true);

                if(isHalfDayLate) {
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setIssueDescription("EMPLOYEE WAS MORE THAN 4 HOURS LATE");
                }
            }

            if(actualEndTime != null) {
                if(!isLate && !isHalfDayLate &&(expectedStartTime.isBefore(actualStartTime) && expectedEndTime.isAfter(actualEndTime))) {
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                }
            }

            attendance.setDate(processDate);
            attendance.setArrivalDate(inOut.getPunchTime());
            attendance.setArrivalTime(inOut.getPunchTypeTime());
            attendance.setTerminalId(inOut.getTerminalId());
            attendance.setIsManual(true);

            if(actualEndTime != null) {
                attendance.setLeftTime(inOutLatest.getPunchTypeTime());
            }

            if(inOutLatest != null) {
                attendance.setTerminalId(attendance.getTerminalId() + " - " + inOutLatest.getTerminalId());
            }

            employeeDetails.add(createEmployeeDetail(employee, team, attendance, inOut, inOutLatest, shiftTime, isRotationShift));
            return attendance;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error processing employee {}: {}", employee.getEmployeeId(), e.getMessage());
            return null;
        }
    }

    private Attendance createAbsentAttendance(Employee employee, Team team, Date date,
                                              String shiftTime, boolean isRotationShift,
                                              List<EmployeeAttendanceDetail> employeeDetails, InOut inOutLatest) {
        Attendance attendance = buildBaseAttendance(employee, team, date, shiftTime, isRotationShift);
        attendance.setPublicId(UUID.randomUUID().toString());
        attendance.setDate(helper.getYesterdayDate());
        attendance.setArrivalDate(helper.getYesterdayDate());
        attendance.setRosterType(RosterType.NORMAL);
        attendance.setAttendanceType(AttendanceType.ABSENT);
        attendance.setDueDateForUA(helper.getDueDate());
        attendance.setHasIssues(true);
        if(inOutLatest != null){
            if(fullDayLeaveTime.isAfter(inOutLatest.getPunchTypeTime()))
                attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
            attendance.setIsUnauthorized(true);
            attendance.setLeftTime(inOutLatest.getPunchTypeTime());
            attendance.setAttendanceType(AttendanceType.NONE);
        }
        else{
            attendance.setIssueDescription("GOING ABSENT DUE TO NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE THE DUE DATE.");
        }
        employeeDetails.add(EmployeeAttendanceDetail.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getName())
                .teamId(team.getId())
                .shiftTime(shiftTime)
                .isRotationShift(isRotationShift)
                .attendanceStatus("ABSENT")
                .build());
        return attendance;
    }

    private Attendance buildBaseAttendance(Employee employee, Team team, Date date,
                                           String shiftTime, boolean isRotationShift) {
        return Attendance.builder()
                .publicId(UUID.randomUUID().toString())
                .date(date)
                .employeeId(employee.getEmployeeId())
                .rosterType(RosterType.NORMAL)
                .attendanceType(AttendanceType.NONE)
                .isLate(false)
                .build();
    }

    private EmployeeAttendanceDetail createEmployeeDetail(Employee employee, Team team,
                                                          Attendance attendance, InOut inOut, InOut inOutLatest, String shiftTime, Boolean isRotationShift) {
        String status = "NONE";
        if (attendance.getAttendanceType() == AttendanceType.FULL_DAY) {
            status = "FULL_DAY";
        } else if (attendance.getAttendanceType() == AttendanceType.HALF_DAY) {
            status = "HALF_DAY";
        } else if (attendance.getAttendanceType() == AttendanceType.ABSENT) {
            status = "ABSENT";
        } else if (Boolean.TRUE.equals(attendance.getIsLate())) {
            status = attendance.getAttendanceType() == AttendanceType.HALF_DAY ? "HALF_DAY" : "LATE";
        }

        String arrivalTime = null;
        String leftTime = null;

        if (inOut != null && inOut.getPunchTypeTime() != null) {
            arrivalTime = inOut.getPunchTypeTime().toString();
        }

        if (inOutLatest != null && inOutLatest.getPunchTypeTime() != null) {
            leftTime = inOutLatest.getPunchTypeTime().toString();
        }

        long late = 0L;
        if (inOut != null && inOut.getPunchTypeTime() != null &&
                inOutLatest != null && inOutLatest.getPunchTypeTime() != null) {
            late = ChronoUnit.MINUTES.between(inOut.getPunchTypeTime(), inOutLatest.getPunchTypeTime());
        }

        return EmployeeAttendanceDetail.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getName())
                .teamId(team.getId())
                .shiftTime(shiftTime)
                .isRotationShift(isRotationShift)
                .arrivalTime(arrivalTime)
                .leftTime(leftTime)
                .attendanceStatus(status)
                .lateMinutes(late)
                .build();
    }

    public Roster getMonthlyAttendance(int month, int year) {
        Optional<Roster> opt = rosterRepository.findByMonthAndYear(month, year);
        return opt.orElse(null);
    }

    public Page<RosterAttendance> getRosterAllAttendance(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return rosterAttendanceRepository.findAll(pageable);
    }

    public List<Attendance> getEmployeeAttendance(String employeeId, Date startDate, Date endDate) {
        return Collections.emptyList();
    }

    public Map<String, TeamAttendanceSummary> getRTeamAttendanceSummary(String dateStr) {
        Optional<RosterAttendance> rosterAttendanceOpt = rosterAttendanceRepository.findByDate(dateStr);
        return rosterAttendanceOpt.map(RosterAttendance::getTeamAttendanceSummary).orElse(Collections.emptyMap());
    }

    public List<EmployeeAttendanceDetail> getRAttendanceSummary(String dateStr) {
        Optional<RosterAttendance> rosterAttendanceOpt = rosterAttendanceRepository.findByDate(dateStr);
        return rosterAttendanceOpt.map(RosterAttendance::getEmployeeAttendanceDetails).orElse(Collections.emptyList());
    }

    public Page<Attendance> getAttendanceSummary(String dateStr, int page, int size) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        Pageable pageable = PageRequest.of(page, size);
        try {
            Date date = helper.stripTimeFromDate(dateFormat.parse(dateStr));
            return attendanceRepository.findByDate(date, pageable);
        } catch (ParseException e) {
            return Page.empty(pageable);
        }
    }

    public RosterAttendance getRoster(String dateStr, int page, int size) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Optional<RosterAttendance> rosterOpt = rosterAttendanceRepository.findByDate(formattedDate);
            return rosterOpt.orElse(null);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}. Expected format: yyyy-MM-dd", dateStr, e);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving roster for date: {}", dateStr, e);
            return null;
        }
    }
}