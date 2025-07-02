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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.slt.radio.rosterservice.messaging.MessageProducerService;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final int LATE_THRESHOLD_MINUTES = 15;
    private static final int HALF_DAY_THRESHOLD_HOURS = 4;

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocal<SimpleDateFormat> ALT_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy"));

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

    @Transactional
    public void processDutyAttendances() {
        DutyRoster duty = dutyRosterRepository.findByIsActive(true).orElse(null);
        if (duty == null) return;

        LocalDate today = LocalDate.now();
        List<Attendance> attendancesToSave = Collections.synchronizedList(new ArrayList<>());

        duty.getDailyDuties().forEach(dailyDuty -> {
            if (!dailyDuty.getDate().equals(today) || !dailyDuty.getDayOfWeek().equals(today.getDayOfWeek())) return;

            dailyDuty.getTimeSlots().forEach(timeSlot -> {
                timeSlot.getAssignedEmployees().parallelStream().forEach(emId -> {
                    try {
                        String cleanEmId = cleanEmployeeId(emId);
                        if (cleanEmId.isEmpty()) {
                            log.warn("Invalid employee ID format: {}", emId);
                            return;
                        }

                        Optional<EmployeeArchive> employee = employeeArchiveRepository.findByEmployeeId(cleanEmId);
                        if (employee.isEmpty()) {
                            return;
                        }

                        // Use proper date without time components
                        Date processDate = stripTimeFromDate(helper.getYesterdayDate());

                        Optional<InOut> earliestPunchIn = inOutRepository.findTopByEmployeeIdAndDateOrderByPunchTimeAsc(
                                employee.get().getSltId(),
                                processDate
                        );
                        Optional<InOut> latestPunchIn = inOutRepository.findTopByEmployeeIdAndDateOrderByPunchTimeDesc(
                                employee.get().getSltId(),
                                processDate
                        );
                        if (earliestPunchIn.isEmpty()) {
                            log.debug("No attendance data for employee: {}", cleanEmId);
                            return;
                        }

                        InOut inOut = earliestPunchIn.get();
                        InOut inOutLatest = latestPunchIn.orElse(null);

                        LocalTime time = inOut.getPunchTypeTime();
                        LocalTime startTime = timeSlot.getStartTime();

                        Duration duration = Duration.between(startTime, time);
                        long hoursLate = duration.toHours();

                        Attendance attendance = new Attendance();
                        if(startTime.isBefore(time))
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);

                        if (hoursLate <= 0)
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);

                        if((inOut == null && inOutLatest != null) || (inOut != null && inOutLatest == null)){
                            attendance.setIsUnauthorized(true);
                            attendance.setHasIssues(true);
                            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
                        }

                        attendance.setPublicId(UUID.randomUUID().toString());
                        attendance.setEmployeeId(cleanEmId);
                        attendance.setTerminalId(inOut.getTerminalId());
                        attendance.setDate(processDate);

                        attendance.setArrivalDate(inOut.getPunchTime());
                        attendance.setArrivalTime(inOut.getPunchTypeTime());
                        if(inOutLatest != null) attendance.setLeftTime(inOutLatest.getPunchTypeTime());
                        attendance.setTerminalId(inOut.getTerminalId() + " - " + inOutLatest.getTerminalId());
                        if(startTime.isAfter(inOut.getPunchTypeTime())){
                            attendance.setIsLate(true);
                        }
                        attendancesToSave.add(attendance);

                    } catch (Exception e) {
                        log.error("Error processing employee {}: {}", emId, e.getMessage());
                    }
                });
            });
        });

        if (!attendancesToSave.isEmpty()) {
            List<Attendance> safeList = new ArrayList<>(attendancesToSave);

            List<String> existingIds = attendanceRepository.findExistingAttendances(
                    safeList.stream()
                            .map(Attendance::getEmployeeId)
                            .collect(Collectors.toList()),
                    stripTimeFromDate(new Date())
            ).stream().filter(Objects::nonNull).map(Attendance::getEmployeeId).toList();

            List<Attendance> uniqueAttendances = safeList.stream()
                    .filter(a -> !existingIds.contains(a.getEmployeeId()))
                    .collect(Collectors.toList());

            if (!uniqueAttendances.isEmpty()) {
                List<Attendance> attendances = attendanceRepository.saveAll(uniqueAttendances);
                attendances.forEach(attendance -> {
                    messageProducerService.sendMessage("roster.queue", attendance);
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


    private Date stripTimeFromDate(Date date) {
        if (date == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
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

                assert parsedTime != null;
                InOut inOut = InOut.builder()
                        .employeeId(employeeId)
                        .date(stripTimeFromDate(helper.getYesterdayDate()))
                        .punchTime(stripTimeFromDate(logDate))
                        .punchTypeTime(parsedTime)
                        .terminalId(log.getTerminalId())
                        .inOutValue("IN".equals(log.getInOut().trim()) ? 1 : 0)
                        .inOutType(inOutType)
                        .etlRunTime(new Date())
                        .build();

                boolean isDuplicate = checkForDuplicateInOut(inOut);

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

    private boolean checkForDuplicateInOut(InOut newInOut) {
        try {
            List<InOut> existingEntries = inOutRepository.findByEmployeeIdAndDateAndPunchTime(
                    newInOut.getEmployeeId(),
                    newInOut.getDate(),
                    newInOut.getPunchTime());

            return existingEntries.stream()
                    .anyMatch(existing -> Objects.equals(existing.getPunchTypeTime(), newInOut.getPunchTypeTime()));

        } catch (Exception e) {
            logger.error("Error checking for duplicate InOut: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public RosterAttendance processAttendanceForDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.get().parse(dateStr);
            Date processDate = stripTimeFromDate(date);

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
            String dayPart = dateStr.split("-")[2];
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
                String dayPart = dateStr.split("-")[2];
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

                teamEmployees.parallelStream().forEach(employee -> {
                    Attendance attendance = processEmployee(employee, dateStr, shiftTime, team, isRotationShift, employeeDetails);
                    if (attendance != null) {
                        attendances.add(attendance);
                    }
                });

                if (!attendances.isEmpty()) {
                    List<Attendance> attendances_save = attendanceRepository.saveAll(attendances);
                    attendances_save.forEach(attendance -> {
                        messageProducerService.sendMessage("roster.queue", attendance);
                    });
                }

                TeamAttendanceSummary summary = createTeamSummary(team, shiftTime, isRotationShift, attendances, teamEmployees.size());
                teamSummaries.put(team.getId(), summary);
            });
        });
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

    private Attendance processEmployee(Employee employee, String dateStr, String shiftTime,
                                       Team team, boolean isRotationShift,
                                       List<EmployeeAttendanceDetail> employeeDetails) {
        try {
            Date processDate = stripTimeFromDate(helper.getYesterdayDate());

            EmployeeArchive employeeArchive = employeeArchiveRepository.findByEmployeeId(employee.getEmployeeId()).orElse(null);
            if (employeeArchive == null) return null;

            String cleanId = cleanEmployeeId(employeeArchive.getSltId());

            /*List<InOut> inOuts = inOutRepository.findByEmployeeIdAndDate(cleanId, processDate);

            InOut inOut = InOutFilterHelper.getEarliestInOutForShift(inOuts, shiftTime);*/

            Optional<InOut> earliestPunchIn = inOutRepository.findTopByEmployeeIdAndDateOrderByPunchTimeAsc(
                    employeeArchive.getSltId(),
                    processDate
            );

            Optional<InOut> latestPunchIn = inOutRepository.findTopByEmployeeIdAndDateOrderByPunchTimeDesc(
                    employeeArchive.getSltId(),
                    processDate
            );


            InOut inOut = earliestPunchIn.orElse(null);
            InOut inOutLatest = latestPunchIn.orElse(null);

            if (inOut == null) {
                return createAbsentAttendance(employee, team, processDate, shiftTime, isRotationShift, employeeDetails);
            }

            String[] shiftTimeParts = shiftTime.split("-");
            if (shiftTimeParts.length != 2) {
                log.error("Invalid shift time format: {}", shiftTime);
                return null;
            }

            LocalTime expectedStartTime;
            try {
                expectedStartTime = parseTimeString(shiftTimeParts[0].trim());
                if (expectedStartTime == null) {
                    log.error("Could not parse expected start time: {}", shiftTimeParts[0]);
                    return null;
                }
            } catch (Exception e) {
                log.error("Error parsing expected start time '{}': {}", shiftTimeParts[0], e.getMessage());
                return null;
            }

            Attendance attendance = buildBaseAttendance(employee, team, processDate, shiftTime, isRotationShift);

            if((inOut == null && inOutLatest != null) || (inOut != null && inOutLatest == null)){
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
            }

            attendance.setArrivalDate(inOut.getPunchTime());
            attendance.setTerminalId(inOut.getTerminalId() + " - " + inOutLatest.getTerminalId());

            LocalTime actualStartTime = inOut.getPunchTypeTime();
            attendance.setArrivalTime(inOut.getPunchTypeTime());

            long lateMinutes = Duration.between(expectedStartTime, actualStartTime).toMinutes();
            if (lateMinutes > LATE_THRESHOLD_MINUTES) {
                attendance.setIsLate(true);
                if (lateMinutes > HALF_DAY_THRESHOLD_HOURS * 60) {
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                } else {
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                }
            } else {
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
            }

            if(expectedStartTime.isAfter(actualStartTime)){
                attendance.setIsLate(true);
            }

            if(inOutLatest != null) attendance.setLeftTime(inOutLatest.getPunchTypeTime());

            employeeDetails.add(createEmployeeDetail(employee, team, attendance, inOut, shiftTime, isRotationShift));
            return attendance;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error processing employee {}: {}", employee.getEmployeeId(), e.getMessage());
            return null;
        }
    }

    private Attendance createAbsentAttendance(Employee employee, Team team, Date date,
                                              String shiftTime, boolean isRotationShift,
                                              List<EmployeeAttendanceDetail> employeeDetails) {
        Attendance attendance = buildBaseAttendance(employee, team, date, shiftTime, isRotationShift);
        attendance.setAttendanceType(AttendanceType.ABSENT);
        attendance.setDueDateForUA(helper.getDueDate());
        attendance.setHasIssues(true);
        attendance.setIssueDescription("GOING ABSENT DUE TO NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE THE DUE DATE.");
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
                .attendanceType(AttendanceType.FULL_DAY)
                .isLate(false)
                .build();
    }

    private EmployeeAttendanceDetail createEmployeeDetail(Employee employee, Team team,
                                                          Attendance attendance, InOut inOut, String shiftTime, Boolean isRotationShift) {
        String status = "PRESENT";
        if (attendance.getAttendanceType() == AttendanceType.ABSENT) {
            status = "ABSENT";
        } else if (Boolean.TRUE.equals(attendance.getIsLate())) {
            status = attendance.getAttendanceType() == AttendanceType.HALF_DAY ? "HALF_DAY" : "LATE";
        }

        String arrivalTime = null;
        String leftTime = null;

        if (inOut != null && inOut.getPunchTypeTime() != null) {
            arrivalTime = inOut.getPunchTypeTime().toString();
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
            Date date = stripTimeFromDate(dateFormat.parse(dateStr));
            return attendanceRepository.findByDate(date, pageable);
        } catch (ParseException e) {
            return Page.empty(pageable);
        }
    }

    public RosterAttendance getRoster(String dateStr, int page, int size) {
        Optional<RosterAttendance> rosterOpt = rosterAttendanceRepository.findByDate(dateStr);
        return rosterOpt.orElse(null);
    }
}