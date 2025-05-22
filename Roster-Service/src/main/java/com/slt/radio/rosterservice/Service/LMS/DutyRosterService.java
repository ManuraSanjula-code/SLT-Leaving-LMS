package com.slt.radio.rosterservice.Service.LMS;

import com.slt.radio.rosterservice.Model.Second.DailyDuty;
import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Model.Second.TimeSlot;
import com.slt.radio.rosterservice.Repo.DutyRosterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DutyRosterService {

    @Autowired
    private DutyRosterRepository dutyRosterRepository;

    public DutyRoster saveRoster(DutyRoster roster) {
        roster.setUpdatedDate(LocalDate.now());
        return dutyRosterRepository.save(roster);
    }

    public List<DutyRoster> getAllRosters() {
        return dutyRosterRepository.findAll();
    }

    public Optional<DutyRoster> getRosterById(String id) {
        return dutyRosterRepository.findById(id);
    }

    public Optional<DutyRoster> getRosterByWeek(LocalDate weekStartingDate) {
        return dutyRosterRepository.findByWeekStartingDate(weekStartingDate);
    }

    public List<DutyRoster> getRostersByEmployee(String employeeId) {
        return dutyRosterRepository.findByEmployeeId(employeeId);
    }

    public DutyRoster processExcelFile(MultipartFile file, String rosterName, LocalDate weekStartingDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Parse the Excel structure similar to your file
            List<DailyDuty> dailyDuties = parseExcelToDailyDuties(sheet, weekStartingDate);

            DutyRoster roster = new DutyRoster(weekStartingDate, rosterName, dailyDuties);
            return saveRoster(roster);
        }
    }

    private List<DailyDuty> parseExcelToDailyDuties(Sheet sheet, LocalDate weekStart) {
        List<DailyDuty> dailyDuties = new ArrayList<>();

        // Assuming your Excel structure:
        // Row 0: Headers (Duty Turn, Mon, Tue, Wed, Thu, Fri, Sat, Sun)
        // Row 1: 06:00-14:00 shift data
        // Row 2: Additional employees (if any)
        // Row 3: 14:00-22:00 shift data

        Row headerRow = sheet.getRow(0);
        Row morningShiftRow = sheet.getRow(1);
        Row additionalEmployeesRow = sheet.getRow(2);
        Row eveningShiftRow = sheet.getRow(3);

        // Days of week (columns 1-7 represent Mon-Sun)
        DayOfWeek[] daysOfWeek = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            int columnIndex = dayIndex + 1; // Skip first column (Duty Turn)
            LocalDate dayDate = weekStart.plusDays(dayIndex);

            List<TimeSlot> timeSlots = new ArrayList<>();

            // Morning shift (06:00-14:00)
            List<String> morningEmployees = extractEmployeesFromCells(
                    morningShiftRow, additionalEmployeesRow, columnIndex);
            if (!morningEmployees.isEmpty()) {
                timeSlots.add(new TimeSlot(
                        LocalTime.of(6, 0),
                        LocalTime.of(14, 0),
                        morningEmployees,
                        "MORNING"
                ));
            }

            // Evening shift (14:00-22:00)
            List<String> eveningEmployees = extractEmployeesFromCell(eveningShiftRow, columnIndex);
            if (!eveningEmployees.isEmpty()) {
                timeSlots.add(new TimeSlot(
                        LocalTime.of(14, 0),
                        LocalTime.of(22, 0),
                        eveningEmployees,
                        "EVENING"
                ));
            }

            if (!timeSlots.isEmpty()) {
                dailyDuties.add(new DailyDuty(daysOfWeek[dayIndex], dayDate, timeSlots));
            }
        }

        return dailyDuties;
    }

    private List<String> extractEmployeesFromCells(Row primaryRow, Row additionalRow, int columnIndex) {
        List<String> employees = new ArrayList<>();

        // Get primary employee
        Cell primaryCell = primaryRow.getCell(columnIndex);
        if (primaryCell != null && !isEmptyCell(primaryCell)) {
            employees.add(getCellValueAsString(primaryCell));
        }

        // Get additional employee (if exists)
        if (additionalRow != null) {
            Cell additionalCell = additionalRow.getCell(columnIndex);
            if (additionalCell != null && !isEmptyCell(additionalCell)) {
                employees.add(getCellValueAsString(additionalCell));
            }
        }

        return employees;
    }

    private List<String> extractEmployeesFromCell(Row row, int columnIndex) {
        List<String> employees = new ArrayList<>();
        Cell cell = row.getCell(columnIndex);

        if (cell != null && !isEmptyCell(cell)) {
            employees.add(getCellValueAsString(cell));
        }

        return employees;
    }

    private boolean isEmptyCell(Cell cell) {
        return cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    public void deleteRoster(String id) {
        dutyRosterRepository.deleteById(id);
    }

    // Helper method to get employee schedule for a specific week
    public Map<String, List<TimeSlot>> getEmployeeScheduleForWeek(String employeeId, LocalDate weekStart) {
        Optional<DutyRoster> rosterOpt = dutyRosterRepository.findByWeekStartingDate(weekStart);
        Map<String, List<TimeSlot>> schedule = new HashMap<>();

        if (rosterOpt.isPresent()) {
            DutyRoster roster = rosterOpt.get();
            for (DailyDuty dailyDuty : roster.getDailyDuties()) {
                List<TimeSlot> employeeSlots = dailyDuty.getTimeSlots().stream()
                        .filter(slot -> slot.getAssignedEmployees().contains(employeeId))
                        .collect(Collectors.toList());

                if (!employeeSlots.isEmpty()) {
                    schedule.put(dailyDuty.getDayOfWeek().toString(), employeeSlots);
                }
            }
        }

        return schedule;
    }
}
