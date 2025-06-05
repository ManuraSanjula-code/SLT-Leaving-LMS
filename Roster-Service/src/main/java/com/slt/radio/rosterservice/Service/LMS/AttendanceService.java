package com.slt.radio.rosterservice.Service.LMS;

import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import com.slt.radio.rosterservice.Model.One.Employeee.EmployeeArchive;
import com.slt.radio.rosterservice.Model.One.LMS.*;
import com.slt.radio.rosterservice.Model.One.Roster;
import com.slt.radio.rosterservice.Model.One.Shift.ShiftAssignment;
import com.slt.radio.rosterservice.Model.One.Shift.ShiftRoster;
import com.slt.radio.rosterservice.Model.One.Teamm.Team;
import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Repo.*;
import com.slt.radio.rosterservice.Utils.InOutFilterHelper;
import com.slt.radio.rosterservice.Utils.ShiftBasedInOutFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final int LATE_THRESHOLD_MINUTES = 15; // 15 minutes grace period
    private static final int HALF_DAY_THRESHOLD_HOURS = 4; // 4 hours late considered as half day
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat ALT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final InOutRepository inOutRepository;
    private final AttendanceRepository attendanceRepository;
    private final RosterAttendanceRepository rosterAttendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final ShiftRosterRepository shiftRosterRepository;
    private final RosterRepository rosterRepository;
    private final EmployeeArchiveRepository employeeArchiveRepository;
    private final DutyRosterRepository dutyRosterRepository;

    public void processDutyAttendances() {
        // Fetch duty roster within transaction
        DutyRoster duty = dutyRosterRepository.findByIsActive(true).orElse(null);
        if (duty == null) return;

        LocalDate today = LocalDate.now();
        List<Attendance> attendancesToSave = new ArrayList<>();

        // Process daily duties sequentially (parallelism at employee level)
        duty.getDailyDuties().forEach(dailyDuty -> {
            if (!dailyDuty.getDate().equals(today)) return;

            dailyDuty.getTimeSlots().forEach(timeSlot -> {
                // Process employees in parallel with proper ID handling
                timeSlot.getAssignedEmployees().parallelStream().forEach(emId -> {

                    try {
                        // Clean and validate employee ID
                        String cleanEmId = cleanEmployeeId(emId);
                        if (cleanEmId.isEmpty()) {
                            log.warn("Invalid employee ID format: {}", emId);
                            return;
                        }

                        // Verify employee exists and is on roster
                        Optional<EmployeeArchive> employee = employeeArchiveRepository.findByEmployeeId(cleanEmId);
                        if (employee.isEmpty()) {
                            return;
                        }

                        // Get attendance data with cleaned ID
                        Optional<InOut> punchInMoaAsc = inOutRepository.findTopByEmployeeIDAndDateOrderByPunchInMoaAsc(
                                employee.get().getSltId(),
                                getYesterdayDate()
                        );

                        if (punchInMoaAsc.isEmpty()) {
                            log.debug("No attendance data for employee: {}", cleanEmId);
                            return;
                        }

                        // Create attendance record
                        InOut inouts = punchInMoaAsc.get();
                        Attendance attendance = new Attendance();
                        attendance.setPublicId(UUID.randomUUID().toString());
                        attendance.setEmployeeID(cleanEmId);
                        attendance.setTerminalID(inouts.getTerminalID());
                        attendance.setDate(new Date());
                        attendance.setArrivalDate(inouts.getPunchInMoa());
                        attendance.setArrivalTime(inouts.getTimeMoa());
                        attendance.setUserId(cleanEmId);
                        attendance.setIsFullDay(true);

                        // Synchronized add to prevent duplicates
                        synchronized (attendancesToSave) {
                            attendancesToSave.add(attendance);
                        }

                    } catch (Exception e) {
                        log.error("Error processing employee {}: {}", emId, e.getMessage());
                    }
                });
            });
        });

        // Batch save with duplicate check
        if (!attendancesToSave.isEmpty()) {
            List<String> existingIds = attendanceRepository.findExistingAttendances(
                    attendancesToSave.stream()
                            .map(Attendance::getEmployeeID)
                            .collect(Collectors.toList()),
                    new Date()
            ).stream().filter(Objects::nonNull).map(Attendance::getEmployeeID).collect(Collectors.toList());

            List<Attendance> uniqueAttendances = attendancesToSave.stream()
                    .filter(a -> !existingIds.contains(a.getEmployeeID()))
                    .collect(Collectors.toList());

            attendanceRepository.saveAll(uniqueAttendances);
        }
    }

    // Helper method to clean employee IDs
    private String cleanEmployeeId(String rawId) {
        if (rawId == null) return "";

        // Remove all whitespace and non-alphanumeric characters
        String cleaned = rawId.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-zA-Z0-9]", "");

        // Convert to uppercase for consistency
        return cleaned.toUpperCase();
    }

    public Optional<ShiftRoster> getAttendance(int year, String month) {
        return shiftRosterRepository.findByMonthAndYear(month, year);
    }

    public synchronized void processAccessLogs(List<AccessLog> accessLogs) {
        Map<String, List<AccessLog>> employeeLogsMap = accessLogs.stream()
                .collect(Collectors.groupingBy(AccessLog::getEmployeeID));

        employeeLogsMap.forEach((employeeId, logs) -> {
            Map<String, List<AccessLog>> dateLogsMap = logs.stream()
                    .collect(Collectors.groupingBy(AccessLog::getLogDate));

            dateLogsMap.forEach((date, dailyLogs) -> {
                try {
                    createInOutFromLogsV1(employeeId, date, dailyLogs);
                } catch (ParseException e) {
                    log.error("Error parsing date/time for employee: {} on date: {}", employeeId, date, e);
                }
            });
        });
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

    public Date getYesterdayDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return removeTimeFromDate(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    // Enhanced time parsing with validation and format handling
    private LocalTime parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            log.warn("Empty or null time string provided");
            return null;
        }

        try {
            // Clean the time string
            String cleanTime = timeStr.trim();

            // Handle different time formats
            if (cleanTime.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                // Format: HH:mm:ss - extract just HH:mm
                String[] parts = cleanTime.split(":");
                cleanTime = parts[0] + ":" + parts[1];
            }

            // Parse with HH:mm format
            return LocalTime.parse(cleanTime, TIME_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse time string '{}': {}", timeStr, e.getMessage());
            return null;
        }
    }

    // Safe hour extraction with validation
    private int extractHourSafely(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            log.warn("Empty or null time string for hour extraction");
            return -1; // Invalid hour indicator
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

    // Additional utility method for data validation
    private boolean isValidTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }

        String cleanTime = timeStr.trim();
        // Check for common time patterns
        return cleanTime.matches("^\\d{1,2}:\\d{2}(:\\d{2})?$");
    }

    private void createInOutFromLogs(String employeeId, String dateStr, List<AccessLog> logs) throws ParseException {
        Date date = ALT_DATE_FORMAT.parse(dateStr);
        logs.sort(Comparator.comparing(AccessLog::getLogTime));

        for (AccessLog log : logs) {
            try {
                InOut inOut = InOut.builder()
                        .employeeID(employeeId)
                        .date(date)
                        .build();

                // Parse the time
                Date timeDate = TIME_FORMAT.parse(log.getLogTime());
                String timeStr = log.getLogTime();

                // Extract hour to determine if it's morning or evening
                int hour = extractHourSafely(timeStr);
                if (hour == -1) {
                    logger.warn("Skipping log with invalid time format for employee: {} - time: {}", employeeId, timeStr);
                    continue;
                }
                boolean isMorning = hour < 12; // Before 12:00 is morning

                if ("IN".equals(log.getInOut())) {
                    inOut.setInOut(1);  // IN = 1

                    if (isMorning) {
                        inOut.setPunchInMoa(timeDate);
                        inOut.setTimeMoa(timeStr);
                        inOut.setIsMorning(true);
                    } else {
                        inOut.setPunchInEv(timeDate);
                        inOut.setTimeEve(timeStr);
                        inOut.setIsEvening(true);
                    }
                } else if ("OUT".equals(log.getInOut())) {
                    inOut.setInOut(-1);  // OUT = -1

                    if (isMorning) {
                        inOut.setPunchInMoa(timeDate);
                        inOut.setTimeMoa(timeStr);
                        inOut.setIsMorning(true);
                    } else {
                        inOut.setPunchInEv(timeDate);
                        inOut.setTimeEve(timeStr);
                        inOut.setIsEvening(true);
                    }
                }

                List<InOut> existingEntries = inOutRepository.findByEmployeeIDAndDate(
                        inOut.getEmployeeID(),
                        inOut.getDate());

                InOut savedInOut = null;
                if (existingEntries.isEmpty() || existingEntries == null) {
                    savedInOut = inOutRepository.save(inOut);
                } else {
                    for (InOut existing : existingEntries) {
                        System.out.println("existing: " + !existing.equals(inOut));
                        if (!existing.equals(inOut)) {
                            savedInOut = inOutRepository.save(inOut);
                        }
                    }
                }
                logger.info("Saved InOut record: {}", savedInOut);

            } catch (ParseException e) {
                logger.error("Error parsing time for employee: {} on log: {}", employeeId, log, e);
            }
        }
    }

    // Updated createInOutFromLogsV1 method with better error handling
    private synchronized void createInOutFromLogsV1(String employeeId, String dateStr, List<AccessLog> logs) throws ParseException {
        if (logs == null || logs.isEmpty()) {
            log.warn("No logs provided for employee: {}", employeeId);
            return;
        }

        Date date = ALT_DATE_FORMAT.parse(dateStr);
        logs.sort(Comparator.comparing(AccessLog::getLogTime));

        for (AccessLog log : logs) {
            try {
                String timeStr = log.getLogTime();

                // Validate time string
                if (!isValidTimeFormat(timeStr)) {
                    logger.warn("Invalid time format in log for employee: {} - time: {}", employeeId, timeStr);
                    continue;
                }

                // Extract hour safely
                int hour = extractHourSafely(timeStr);
                if (hour == -1) {
                    logger.warn("Skipping log with invalid time format for employee: {} - time: {}", employeeId, timeStr);
                    continue;
                }

                boolean isMorning = hour < 12;

                InOut inOut = InOut.builder()
                        .employeeID(employeeId)
                        .date(getYesterdayDate())
                        .etl_RunTime(new Date())
                        .build();

                // Clean time string for storage (remove seconds if present)
                String cleanTimeStr = timeStr.trim();
                if (cleanTimeStr.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                    String[] parts = cleanTimeStr.split(":");
                    cleanTimeStr = parts[0] + ":" + parts[1];
                }

                if ("IN".equals(log.getInOut())) {
                    inOut.setInOut(1);
                    if (isMorning) {
                        inOut.setPunchInMoa(getYesterdayDate());
                        inOut.setTimeMoa(cleanTimeStr);
                        inOut.setIsMorning(true);
                    } else {
                        inOut.setPunchInEv(getYesterdayDate());
                        inOut.setTimeEve(cleanTimeStr);
                        inOut.setIsEvening(true);
                    }
                } else if ("OUT".equals(log.getInOut())) {
                    inOut.setInOut(-1);
                    if (isMorning) {
                        inOut.setPunchInMoa(getYesterdayDate());
                        inOut.setTimeMoa(cleanTimeStr);
                        inOut.setIsMorning(true);
                    } else {
                        inOut.setPunchInEv(getYesterdayDate());
                        inOut.setTimeEve(cleanTimeStr);
                        inOut.setIsEvening(true);
                    }
                }

                // Check for duplicates properly
                boolean isDuplicate = false;
                List<InOut> existingEntries = inOutRepository.findByEmployeeIDAndDate(
                        inOut.getEmployeeID(),
                        inOut.getDate());

                if (existingEntries != null && !existingEntries.isEmpty()) {
                    for (InOut existing : existingEntries) {
                        // Compare specific fields that determine a duplicate
                        if (isSameInOutRecord(existing, inOut)) {
                            isDuplicate = true;
                            logger.info("Duplicate record found for employee: {} on date: {} with time: {}",
                                    employeeId, dateStr, cleanTimeStr);
                            break;
                        }
                    }
                }

                // Only save if not a duplicate
                if (!isDuplicate) {
                    InOut savedInOut = inOutRepository.save(inOut);
                    logger.info("Saved InOut record: {}", savedInOut);
                }
            } catch (Exception e) {
                logger.error("Error processing log for employee: {} - log: {} - error: {}",
                        employeeId, log, e.getMessage());
            }
        }
    }

    private boolean isSameInOutRecord(InOut existing, InOut newRecord) {
        // Morning records
        if (existing.getIsMorning() && newRecord.getIsMorning()) {
            return existing.getTimeMoa() != null &&
                    existing.getTimeMoa().equals(newRecord.getTimeMoa()) &&
                    existing.getInOut().equals(newRecord.getInOut());
        }

        // Evening records
        if (existing.getIsEvening() && newRecord.getIsEvening()) {
            return existing.getTimeEve() != null &&
                    existing.getTimeEve().equals(newRecord.getTimeEve()) &&
                    existing.getInOut().equals(newRecord.getInOut());
        }

        // If one is morning and one is evening, they're different records
        return false;
    }

    public RosterAttendance processAttendanceForDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);

            // Get month and year from date
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
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
            dayPart = dayPart.split("0")[1];

            // Check if the date exists in the roster
            if (!shiftRoster.getDates().contains(dayPart)) {
                log.error("Date: {} not found in shift roster", dateStr);
                return null;
            }

            ConcurrentHashMap<String, TeamAttendanceSummary> teamSummaries = new ConcurrentHashMap<>();
            List<EmployeeAttendanceDetail> employeeDetails = new ArrayList<>();

            processShifts(dateStr, shiftRoster.getDutyTurn(), teamSummaries, employeeDetails, false);

            // Process rotation shifts (OT)
            if (shiftRoster.getDutyTurn().containsKey("ROT")) {
                processShifts(dateStr, Collections.singletonMap("ROT", shiftRoster.getDutyTurn().get("ROT")),
                        teamSummaries, employeeDetails, true);
            }

            // Create and save RosterAttendance
            RosterAttendance rosterAttendance = RosterAttendance.builder()
                    .date(dateStr)
                    .month(monthNumber)
                    .year(year)
                    .teamAttendanceSummary(teamSummaries)
                    .employeeAttendanceDetails(employeeDetails)
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
                dayPart = dayPart.split("0")[1];
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

                // Process employees in parallel
                List<Attendance> attendances = teamEmployees.parallelStream()
                        .map(employee -> processEmployee(employee, dateStr, shiftTime, team, isRotationShift, employeeDetails))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!attendances.isEmpty()) {
                    attendanceRepository.saveAll(attendances);
                }

                // Create team summary
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

        for (Attendance attendance : attendances) {
            if (attendance.getIsAbsent()) {
                absentCount++;
            } else if (attendance.getIsLate()) {
                lateCount++;
                if (attendance.getIsHalfDay()) {
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

    // Updated processEmployee method with better time parsing
    private Attendance processEmployee(Employee employee, String dateStr, String shiftTime,
                                       Team team, boolean isRotationShift,
                                       List<EmployeeAttendanceDetail> employeeDetails) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            EmployeeArchive employeeArchive = employeeArchiveRepository.findByEmployeeId(employee.getEmployeeId())
                    .orElse(null);
            if (employeeArchive == null) return null;

            String cleanId = cleanEmployeeId(employeeArchive.getSltId());

            List<InOut> inOuts = inOutRepository.findByEmployeeIDAndDate(cleanId, getYesterdayDate());

            InOut inOut = InOutFilterHelper.getEarliestInOutForShift(inOuts, shiftTime);

            if (inOut == null) {
                return createAbsentAttendance(employee, team, date, shiftTime, isRotationShift, employeeDetails);
            }

            // Validate and parse shift time
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

            Attendance attendance = buildBaseAttendance(employee, team, date, shiftTime, isRotationShift);

            // Process attendance logic with improved time parsing
            if (inOut.getIsMorning() && inOut.getTimeMoa() != null) {
                LocalTime actualStartTime = parseTimeString(inOut.getTimeMoa());
                if (actualStartTime != null) {
                    attendance.setArrivalDate(date);
                    attendance.setArrivalTime(inOut.getTimeMoa());

                    long lateMinutes = Duration.between(expectedStartTime, actualStartTime).toMinutes();
                    if (lateMinutes > LATE_THRESHOLD_MINUTES) {
                        attendance.setIsLate(true);
                        if (lateMinutes > HALF_DAY_THRESHOLD_HOURS * 60) {
                            attendance.setIsHalfDay(true);
                        }
                    } else {
                        attendance.setIsFullDay(true);
                    }
                } else {
                    log.warn("Could not parse arrival time for employee: {} - time: {}",
                            employee.getEmployeeId(), inOut.getTimeMoa());
                    attendance.setIsAbsent(true);
                }
            } else {
                attendance.setIsAbsent(true);
            }

            if (inOut.getIsEvening() && inOut.getTimeEve() != null) {
                attendance.setLeftTime(inOut.getTimeEve());
            }

            employeeDetails.add(createEmployeeDetail(employee, team, attendance, inOut));
            return attendance;

        } catch (Exception e) {
            log.error("Error processing employee {}: {}", employee.getEmployeeId(), e.getMessage());
            return null;
        }
    }

    private Attendance createAbsentAttendance(Employee employee, Team team, Date date,
                                              String shiftTime, boolean isRotationShift,
                                              List<EmployeeAttendanceDetail> employeeDetails) {
        Attendance attendance = buildBaseAttendance(employee, team, date, shiftTime, isRotationShift);
        attendance.setIsAbsent(true);
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
                .employeeID(employee.getEmployeeId())
                .teamId(team.getId())
                .shiftCode(team.getShortName() + (isRotationShift ? " ROT" : ""))
                .shiftTime(shiftTime)
                .isOvertimeShift(isRotationShift)
                .build();
    }

    private EmployeeAttendanceDetail createEmployeeDetail(Employee employee, Team team,
                                                          Attendance attendance, InOut inOut) {
        String status = "PRESENT";
        if (attendance.getIsAbsent()) {
            status = "ABSENT";
        } else if (attendance.getIsLate()) {
            status = attendance.getIsHalfDay() ? "HALF_DAY" : "LATE";
        }

        return EmployeeAttendanceDetail.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getName())
                .teamId(team.getId())
                .shiftTime(attendance.getShiftTime())
                .isRotationShift(attendance.getIsOvertimeShift())
                .arrivalTime(inOut.getTimeMoa())
                .leftTime(inOut.getTimeEve())
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
        // Implementation omitted for brevity
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
        dateFormat.setLenient(false);  // For strict date validation
        Pageable pageable = PageRequest.of(page, size);
        try {
            Date date = dateFormat.parse(dateStr);
            return attendanceRepository.findByDate(date, pageable);
        } catch (ParseException e) {
            return Page.empty(pageable);
        }
    }

    public RosterAttendance getRoster(String dateStr, int page, int size) {
        Optional<RosterAttendance> rosterOpt = rosterAttendanceRepository.findByDate(dateStr);
        if (rosterOpt.isPresent())
            return rosterOpt.get();
        else
            return null;
    }
}