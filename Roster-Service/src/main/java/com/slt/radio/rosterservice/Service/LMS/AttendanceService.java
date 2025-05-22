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


    public void processAccessLogs(List<AccessLog> accessLogs) {
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

    public void processAttendanceForDate(DutyRoster roster) {
        roster.getDailyDuties().forEach(day_duty -> {
            day_duty.getTimeSlots().forEach(time -> {
                time.getAssignedEmployees().forEach(emId -> {

                    Optional<EmployeeArchive> employee = employeeArchiveRepository.findByEmployeeId(emId);
                    EmployeeArchive emp = employee.orElse(null);
                    if (emp == null) return;

                    Optional<InOut> inouts = inOutRepository.findTopByEmployeeIDAndDateOrderByPunchInMoaAsc(emId, getYesterdayDate());

                    if(inouts.isEmpty()) return;

                    Attendance attendance = new Attendance();
                    attendance.setPublicId(UUID.randomUUID().toString());
                    attendance.setEmployeeID(emId);
                    attendance.setTerminalID(inouts.get().getTerminalID());
                    attendance.setDate(new Date());
                    attendance.setArrivalDate(inouts.get().getPunchInMoa());
                    attendance.setArrivalTime(inouts.get().getTimeMoa());
                    attendance.setUserId(emp.getUserId());
                    attendance.setIsFullDay(true);

                    attendanceRepository.save(attendance);
                });
            });
        });
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
                int hour = Integer.parseInt(timeStr.split(":")[0]);
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

    private void createInOutFromLogsV1(String employeeId, String dateStr, List<AccessLog> logs) throws ParseException {
        Date date = ALT_DATE_FORMAT.parse(dateStr);
        logs.sort(Comparator.comparing(AccessLog::getLogTime));

        for (AccessLog log : logs) {
            try {
                // Parse the time
                Date timeDate = TIME_FORMAT.parse(log.getLogTime());
                String timeStr = log.getLogTime();

                // Extract hour to determine if it's morning or evening
                int hour = Integer.parseInt(timeStr.split(":")[0]);
                boolean isMorning = hour < 12; // Before 12:00 is morning

                InOut inOut = InOut.builder()
                        .employeeID(employeeId)
                        .date(date)
                        .build();

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
                                    employeeId, dateStr, timeStr);
                            break;
                        }
                    }
                }

                // Only save if not a duplicate
                if (!isDuplicate) {
                    InOut savedInOut = inOutRepository.save(inOut);
                    logger.info("Saved InOut record: {}", savedInOut);
                }
            } catch (ParseException e) {
                logger.error("Error parsing time for employee: {} on log: {}", employeeId, log, e);
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

            Optional<ShiftRoster> shiftRosterOpt = shiftRosterRepository.findByMonthAndYear("January", year);
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

            Map<String, TeamAttendanceSummary> teamSummaries = new HashMap<>();
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
                               Map<String, TeamAttendanceSummary> teamSummaries,
                               List<EmployeeAttendanceDetail> employeeDetails,
                               boolean isRotationShift) {

        shifts.forEach((shiftTime, assignments) -> {

            for (ShiftAssignment assignment : assignments) {

                String dayPart = dateStr.split("-")[2];
                dayPart = dayPart.split("0")[1];

                if (!dayPart.equals(assignment.getDate())) {
                    continue;
                }

                String teamNameOrId = assignment.getTeam();
                boolean isTeamRotation = teamNameOrId.contains("ROT");

                // Skip if we're not processing rotation shifts but the team is rotation
                if (!isRotationShift && isTeamRotation) {
                    continue;
                }

                // Skip if we're processing rotation shifts but the team is not rotation
                if (isRotationShift && !isTeamRotation) {
                    continue;
                }

                // Clean up team ID from ROT suffix if needed
                String teamId = teamNameOrId.replace(" ROT", "");

                Optional<Team> teamOpt = teamRepository.findByShortName(teamId);
                if (teamOpt.isEmpty()) {
                    log.error("Team not found with ID/shortName: {}", teamId);
                    continue;
                }

                Team team = teamOpt.get();

                List<Employee> teamEmployees = employeeRepository.findByTeamId(team.getId());
                if (teamEmployees.isEmpty()) {
                    log.warn("No employees found for team: {}", team.getName());
                    continue;
                }


                // Initialize team attendance summary
                TeamAttendanceSummary teamSummary = teamSummaries.getOrDefault(
                        team.getId(),
                        TeamAttendanceSummary.builder()
                                .teamId(team.getId())
                                .teamName(team.getName())
                                .shiftTime(shiftTime)
                                .totalEmployees(teamEmployees.size())
                                .presentEmployees(0)
                                .lateEmployees(0)
                                .absentEmployees(0)
                                .halfDayEmployees(0)
                                .isRotationShift(isRotationShift)
                                .build()
                );

                // Process each employee in the team
                for (Employee employee : teamEmployees) {
                    try {

                        Date date = DATE_FORMAT.parse(dateStr);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        Date dateWithoutTime = calendar.getTime();
                        // Get InOut record for the employee and date
                        Optional<InOut> inOutOpt = inOutRepository.findTopByEmployeeIDAndDateOrderByPunchInMoaAsc(
                                employee.getEmployeeId(), date);

                        // Get expected shift times
                        String[] shiftTimeParts = shiftTime.split("-");
                        if (shiftTimeParts.length != 2) {
                            log.error("Invalid shift time format: {}", shiftTime);
                            continue;
                        }

                        LocalTime expectedStartTime = LocalTime.parse(shiftTimeParts[0].trim(), TIME_FORMATTER);
                        LocalTime expectedEndTime = LocalTime.parse(shiftTimeParts[1].trim(), TIME_FORMATTER);


                        // Create attendance
                        Attendance attendance = Attendance.builder()
                                .publicId(UUID.randomUUID().toString())
                                .date(dateWithoutTime)
                                .employeeID(employee.getEmployeeId())
                                .teamId(team.getId())
                                .shiftCode(teamNameOrId)
                                .shiftTime(shiftTime)
                                .isOvertimeShift(isRotationShift)
                                .build();

                        EmployeeAttendanceDetail employeeDetail = EmployeeAttendanceDetail.builder()
                                .employeeId(employee.getEmployeeId())
                                .employeeName(employee.getName())
                                .teamId(team.getId())
                                .shiftTime(shiftTime)
                                .isRotationShift(isRotationShift)
                                .build();

                        if (inOutOpt.isPresent()) {
                            InOut inOut = inOutOpt.get();
                            attendance.setTerminalID(inOut.getTerminalID());
                            attendance.setArrivalTime(inOut.getTimeMoa());
                            attendance.setArrivalDate(inOut.getPunchInMoa());
                            // Process arrival time
                            if (inOut.getIsMorning() && inOut.getTimeMoa() != null) {
                                LocalTime actualStartTime = LocalTime.parse(inOut.getTimeMoa(), TIME_FORMATTER);
                                attendance.setArrivalDate(date);
                                attendance.setArrivalTime(inOut.getTimeMoa());
                                employeeDetail.setArrivalTime(inOut.getTimeMoa());

                                // Check if late
                                long lateMinutes = Duration.between(expectedStartTime, actualStartTime).toMinutes();
                                if (lateMinutes > LATE_THRESHOLD_MINUTES) {
                                    attendance.setIsLate(true);
                                    employeeDetail.setLateMinutes(lateMinutes);
                                    teamSummary.setLateEmployees(teamSummary.getLateEmployees() + 1);

                                    // Check if half day (late for more than 4 hours)
                                    if (lateMinutes > HALF_DAY_THRESHOLD_HOURS * 60) {
                                        attendance.setIsHalfDay(true);
                                        teamSummary.setHalfDayEmployees(teamSummary.getHalfDayEmployees() + 1);
                                        employeeDetail.setAttendanceStatus("HALF_DAY");
                                    } else {
                                        employeeDetail.setAttendanceStatus("LATE");
                                    }
                                } else {
                                    attendance.setIsFullDay(true);
                                    teamSummary.setPresentEmployees(teamSummary.getPresentEmployees() + 1);
                                    employeeDetail.setAttendanceStatus("PRESENT");
                                }
                            } else {
                                // No morning punch, mark as absent
                                attendance.setIsAbsent(true);
                                teamSummary.setAbsentEmployees(teamSummary.getAbsentEmployees() + 1);
                                employeeDetail.setAttendanceStatus("ABSENT");
                            }

                            // Process departure time
                            if (inOut.getIsEvening() && inOut.getTimeEve() != null) {
                                attendance.setLeftTime(inOut.getTimeEve());
                                employeeDetail.setLeftTime(inOut.getTimeEve());
                            }
                        } else {
                            // No InOut record, mark as absent
                            attendance.setIsAbsent(true);
                            teamSummary.setAbsentEmployees(teamSummary.getAbsentEmployees() + 1);
                            employeeDetail.setAttendanceStatus("ABSENT");
                        }

                        // Save attendance record
                        attendanceRepository.save(attendance);

                        // Add employee detail to list
                        employeeDetails.add(employeeDetail);
                    } catch (Exception e) {
                        log.error("Error processing attendance for employee: {} on date: {}",
                                employee.getEmployeeId(), dateStr, e);
                    }
                }

                UUID uuid = UUID.randomUUID();

                // Convert UUID to a unique long value
                long uniqueNumber = uuid.getMostSignificantBits() & Long.MAX_VALUE;

                // Update team summary
                teamSummaries.put(String.valueOf(uniqueNumber), teamSummary);
            }
        });
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
