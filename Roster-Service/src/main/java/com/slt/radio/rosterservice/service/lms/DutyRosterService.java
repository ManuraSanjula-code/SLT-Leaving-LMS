package com.slt.radio.rosterservice.service.lms;

import com.slt.radio.rosterservice.documents.second.DailyDuty;
import com.slt.radio.rosterservice.documents.second.DutyRoster;
import com.slt.radio.rosterservice.documents.second.TimeSlot;
import com.slt.radio.rosterservice.repo.DutyRosterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DutyRosterService {

    @Autowired
    private DutyRosterRepository dutyRosterRepository;

    public DutyRoster saveRoster(DutyRoster roster) {
        Optional<DutyRoster> existingRoster = dutyRosterRepository.findByWeekStartingDate(roster.getWeekStartingDate());
        if (existingRoster.isPresent()) {
            throw new IllegalStateException("Roster exists for week starting " + roster.getWeekStartingDate());
        }
        roster.setUpdatedDate(LocalDate.now());
        roster.setActive(true);
        return dutyRosterRepository.save(roster);
    }

    public DutyRoster getTheDuty(String weekDays) {
        LocalDate weekStartingDate = LocalDate.parse(weekDays);
        return dutyRosterRepository.findByWeekStartingDate(weekStartingDate).orElse(null);
    }

    public void deleteRosterForWeek(String week) {
        LocalDate weekStartingDate = LocalDate.parse(week);
        dutyRosterRepository.deleteDutyRosterByWeekStartingDate(weekStartingDate);
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

            List<DailyDuty> dailyDuties;
            if (isNewFormat(sheet)) {
                dailyDuties = parseNewFormatExcel(sheet, weekStartingDate);
            } else {
                dailyDuties = parseOldFormatExcel(sheet, weekStartingDate);
            }

            DutyRoster roster = new DutyRoster(weekStartingDate, rosterName, dailyDuties, true);
            Optional<DutyRoster> latestActiveRoster = dutyRosterRepository.findLatestActiveRoster();
            if (latestActiveRoster.isPresent()) {
                DutyRoster roster_ = latestActiveRoster.get();
                roster_.setActive(false);
                dutyRosterRepository.save(roster_);
            }else{
                dutyRosterRepository.findAll().forEach(roster_->{
                    roster_.setActive(false);
                    dutyRosterRepository.save(roster_);
                });
            }
            return saveRoster(roster);
        }
    }

    private void deactivateAllOtherRosters() {
        dutyRosterRepository.findAll().forEach(r -> {
            r.setActive(false);
            saveRoster(r);
        });
    }

    private boolean isNewFormat(Sheet sheet) {
        // Check for indicators of new format:
        // 1. Look for "This roster is effective from" text
        // 2. Check if morning shift is in row 1 and evening in row 2 without additional row
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING &&
                        cell.getStringCellValue().contains("This roster is effective from")) {
                    return true;
                }
            }
        }

        // Additional check based on structure
        if (sheet.getPhysicalNumberOfRows() >= 3) {
            Row row3 = sheet.getRow(3);
            if (row3 == null || isRowEmpty(row3)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private List<DailyDuty> parseOldFormatExcel(Sheet sheet, LocalDate weekStart) {
        List<DailyDuty> dailyDuties = new ArrayList<>();

        Row headerRow = sheet.getRow(0);
        Row morningShiftRow = sheet.getRow(1);
        Row additionalEmployeesRow = sheet.getRow(2);
        Row eveningShiftRow = sheet.getRow(3);

        DayOfWeek[] daysOfWeek = DayOfWeek.values();

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            int columnIndex = dayIndex + 1;
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

    private List<DailyDuty> parseNewFormatExcel(Sheet sheet, LocalDate weekStart) {
        List<DailyDuty> dailyDuties = new ArrayList<>();

        Row headerRow = sheet.getRow(0);
        Row morningShiftRow = sheet.getRow(1);
        Row eveningShiftRow = sheet.getRow(2);

        DayOfWeek[] daysOfWeek = DayOfWeek.values();

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            int columnIndex = dayIndex + 1;
            LocalDate dayDate = weekStart.plusDays(dayIndex);

            List<TimeSlot> timeSlots = new ArrayList<>();

            // Morning shift (06:00-14:00)
            List<String> morningEmployees = extractEmployeesFromCell(morningShiftRow, columnIndex);
            if (!morningEmployees.isEmpty()) {
                List<String> allMorningEmployees = splitEmployeeIds(morningEmployees);
                timeSlots.add(new TimeSlot(
                        LocalTime.of(6, 0),
                        LocalTime.of(14, 0),
                        allMorningEmployees,
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

    private List<String> splitEmployeeIds(List<String> employeeCells) {
        List<String> result = new ArrayList<>();
        for (String cellValue : employeeCells) {
            if (cellValue.contains(",")) {
                String[] parts = cellValue.split("\\s*,\\s*");
                Collections.addAll(result, parts);
            } else {
                result.add(cellValue);
            }
        }
        return result;
    }

    private List<String> extractEmployeesFromCells(Row primaryRow, Row additionalRow, int columnIndex) {
        List<String> employees = new ArrayList<>();

        if (primaryRow != null) {
            Cell primaryCell = primaryRow.getCell(columnIndex);
            if (primaryCell != null && !isEmptyCell(primaryCell)) {
                employees.add(getCellValueAsString(primaryCell));
            }
        }

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
        if (row == null) {
            return employees;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell != null && !isEmptyCell(cell)) {
            String value = getCellValueAsString(cell);
            if (value.contains(",")) {
                Collections.addAll(employees, value.split("\\s*,\\s*"));
            } else {
                employees.add(value);
            }
        }

        return employees;
    }

    private boolean isEmptyCell(Cell cell) {
        if (cell == null) {
            return true;
        }
        return cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Check if it's a whole number
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((int) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    public void deleteRoster(String id) {
        dutyRosterRepository.deleteById(id);
    }

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