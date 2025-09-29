package com.slt.radio.rosterservice.service.employee;

import com.slt.radio.rosterservice.exception.ExcelProcessingException;
import com.slt.radio.rosterservice.documents.one.employeee.Employee;
import com.slt.radio.rosterservice.documents.one.team.Team;
import com.slt.radio.rosterservice.models.dto.*;
import com.slt.radio.rosterservice.repo.EmployeeRepository;
import com.slt.radio.rosterservice.repo.TeamRepository;
import com.slt.radio.rosterservice.service.roster.ExcelParserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Month;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelProcessingService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TeamService teamService;
    private static final Logger log = LoggerFactory.getLogger(ExcelProcessingService.class);

    // Process Excel file and return RosterDto
    public RosterDto processExcelFile(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Extract month and year
            MonthYearInfo monthYearInfo = extractMonthAndYear(sheet);
            int month = monthYearInfo.getMonth();
            int year = monthYearInfo.getYear();

            log.info("Processing roster for {}/{}", month, year);

            // Find the header row first
            HeaderRowInfo headerRowInfo = findHeaderRow(sheet);

            if (!headerRowInfo.isFound()) {
                throw new ExcelProcessingException("Could not locate header row with employee information columns");
            }

            log.info("Found header row at index: {}", headerRowInfo.getRowIndex());

            // Process teams and employees
            List<TeamRosterDto> teamRosters = processTeamsAndEmployees(sheet, headerRowInfo);

            // Create and return the final roster DTO
            return RosterDto.builder()
                    .month(month)
                    .year(year)
                    .teams(teamRosters)
                    .build();

        } catch (IOException e) {
            throw new ExcelProcessingException("Failed to process Excel file", e);
        }
    }

    // Extract month and year from the Excel sheet
    private MonthYearInfo extractMonthAndYear(Sheet sheet) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        /*int month = 12;
        int year = 2024;*/

        // Look for title rows near the top of the sheet
        for (int i = 0; i < 10; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Try to find the title cell (typically in the first few columns)
            for (int cellIndex = 0; cellIndex < Math.min(5, row.getLastCellNum()); cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell == null || cell.getCellType() != CellType.STRING) continue;

                String value = cell.getStringCellValue().trim();

                // Skip empty cells
                if (value.isEmpty()) continue;

                // Look for roster title patterns
                if (value.contains("Roster") && value.length() > 10) {
                    log.info("Found potential roster title: {}", value);

                    // Extract month
                    for (Month m : Month.values()) {
                        if (value.contains(m.name()) || value.contains(m.name().substring(0, 3))) {
                            month = m.getValue();
                            log.info("Detected month: {}", m.name());
                            break;
                        }
                    }

                    // Extract year with a more flexible pattern (e.g., 2024, 2025)
                    Pattern yearPattern = Pattern.compile("202[0-9]");
                    Matcher matcher = yearPattern.matcher(value);
                    if (matcher.find()) {
                        year = Integer.parseInt(matcher.group());
                        log.info("Detected year: {}", year);
                    }

                    // If we found both month and year, we can stop
                    if (month != 12 || year != 2024) {
                        break;
                    }
                }
            }

            // If we found month or year, stop searching
            if (month != 12 || year != 2024) {
                break;
            }
        }

        // If we couldn't determine from title, try to extract from filename
        if (month == 12 && year == 2024) {
            // Fallback logic here if needed
            log.warn("Could not determine month/year from title, using defaults: {}/{}", month, year);
        }

        return new MonthYearInfo(month, year);
    }

    // Find header row with employee information columns
    private HeaderRowInfo findHeaderRow(Sheet sheet) {
        // Columns we're looking for
        final String[] TARGET_HEADERS = {"Name", "Mobile No", "Sr. No", "Total Shift", "ROT Shift", "Off Day", "D Duty"};

        // Examine rows near the top of the sheet, but not too far down
        for (int rowIndex = 0; rowIndex <= Math.min(50, sheet.getLastRowNum()); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            // Map to track found headers and their column indices
            Map<String, Integer> foundHeaders = new HashMap<>();
            int nameColIndex = -1;

            // Examine each cell in the row
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) continue;

                // Get cell value as string
                String cellValue = getCellStringValue(cell).trim();
                if (cellValue.isEmpty()) continue;

                // Check if this cell matches any of our target headers
                for (String header : TARGET_HEADERS) {
                    if (cellValue.equalsIgnoreCase(header) ||
                            cellValue.contains(header) ||
                            header.contains(cellValue)) {

                        foundHeaders.put(header, colIndex);

                        // Keep track of the "Name" column specifically
                        if (header.equals("Name")) {
                            nameColIndex = colIndex;
                        }

                        break;
                    }
                }
            }

            // If we found the "Name" column and at least 3 other headers, consider this the header row
            if (nameColIndex != -1 && foundHeaders.size() >= 4) {
                log.info("Found header row with {} matching columns", foundHeaders.size());
                return new HeaderRowInfo(true, rowIndex, foundHeaders, nameColIndex);
            }
        }


        log.warn("Could not find header row, using default column mapping");
        Map<String, Integer> defaultMapping = new HashMap<>();
        defaultMapping.put("Name", 2);         // Column C
        defaultMapping.put("Mobile No", 6);    // Column G
        defaultMapping.put("Sr. No", 8);       // Column I
        defaultMapping.put("Total Shift", 9);  // Column J
        defaultMapping.put("ROT Shift", 10);   // Column K
        defaultMapping.put("Off Day", 11);     // Column L
        defaultMapping.put("D Duty", 12);      // Column M

        return new HeaderRowInfo(false, -1, defaultMapping, 2);
    }

    // Process teams and employees from the sheet
    private List<TeamRosterDto> processTeamsAndEmployees(Sheet sheet, HeaderRowInfo headerInfo) {
        List<TeamRosterDto> teamRosters = new ArrayList<>();
        Map<String, List<EmployeeData>> teamEmployees = new HashMap<>();

        // Current section tracking
        String currentTeam = null;
        boolean inDutySection = false;
        int headerRowIndex = headerInfo.getRowIndex();

        // Start processing from the row after headers
        int processStartRow = headerRowIndex > 0 ? headerRowIndex + 1 : 0;

        for (int rowIndex = processStartRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            // Check if this is a team header row
            String potentialTeamName = identifyTeamHeader(row);
            if (potentialTeamName != null) {
                currentTeam = potentialTeamName;
                inDutySection = false;

                // Initialize team if not exists
                teamEmployees.putIfAbsent(currentTeam, new ArrayList<>());
                log.info("Found team: {}", currentTeam);
                continue;
            }

            // Check if this is a duty section header (Day Duty, Night Duty, etc.)
            String dutySection = identifyDutySection(row);
            if (dutySection != null) {
                inDutySection = true;
                log.info("Found duty section: {}", dutySection);
                continue;
            }

            // If we're in a team section but not in a duty section, process employee data
            if (currentTeam != null && !inDutySection) {
                EmployeeData employeeData = extractEmployeeData(row, headerInfo.getHeaderMap());
                if (employeeData != null) {
                    teamEmployees.get(currentTeam).add(employeeData);
                    log.debug("Added employee {} to team {}", employeeData.getName(), currentTeam);
                }
            }
        }

        // If no clear teams were found, check if we have any employees and create a default team
        if (teamEmployees.isEmpty() || teamEmployees.values().stream().allMatch(List::isEmpty)) {
            log.info("No teams with employees found, attempting to scan for employees directly");
            List<EmployeeData> employeesFromScan = scanForEmployees(sheet, headerInfo);

            if (!employeesFromScan.isEmpty()) {
                teamEmployees.put("Default Team", employeesFromScan);
            }
        }

        // Convert the team employees map to TeamRosterDto objects
        for (Map.Entry<String, List<EmployeeData>> entry : teamEmployees.entrySet()) {
            String teamName = entry.getKey();
            List<EmployeeData> employees = entry.getValue();

            if (employees.isEmpty()) {
                log.warn("Team {} has no employees, skipping", teamName);
                continue;
            }

            // Create or get team from database
            String teamId = getOrCreateTeam(teamName);
            List<EmployeeShiftDto> employeeShifts = new ArrayList<>();

            // Process employees for this team
            for (EmployeeData employee : employees) {
                // Create or get employee from database
                String employeeId = getOrCreateEmployee(
                        employee.getName(),
                        employee.getSrNo(),
                        employee.getMobileNo(),
                        employee.getCodeName(),
                        teamId
                );

                // Create employee shift DTO
                EmployeeShiftDto employeeShift = EmployeeShiftDto.builder()
                        .employeeId(employeeId)
                        .name(employee.getName())
                        .mobileNo(employee.getMobileNo())
                        .codeName(employee.getCodeName())
                        .totalShift(employee.getTotalShift())
                        .rotShift(employee.getRotShift())
                        .offDay(employee.getOffDay())
                        .dDuty(employee.getdDuty())
                        .build();

                employeeShifts.add(employeeShift);
            }

            // Create team roster DTO
            TeamRosterDto teamRosterDto = TeamRosterDto.builder()
                    .teamId(teamId)
                    .employees(employeeShifts)
                    .build();

            teamRosters.add(teamRosterDto);
        }

        return teamRosters;
    }

    // Identify team headers in the Excel
    private String identifyTeamHeader(Row row) {
        // Check for team header in first few cells of the row
        for (int i = 0; i < Math.min(5, row.getLastCellNum()); i++) {
            Cell cell = row.getCell(i);
            if (cell == null) continue;

            String value = getCellStringValue(cell).trim();
            if (value.isEmpty()) continue;

            // Team detection criteria:
            // 1. Contains "Team" or "TEAM"
            // 2. Not too long (to avoid regular text)
            // 3. Doesn't contain column headers like "Name"
            // 4. Not exactly "Teams" (which might be a section header)
            if ((value.contains("Team") || value.contains("TEAM")) &&
                    value.length() < 30 &&
                    !value.contains("Name") &&
                    !value.equalsIgnoreCase("Teams")) {
                return value;
            }
        }
        return null;
    }

    // Identify duty sections (Day Duty, Night Duty, etc.)
    private String identifyDutySection(Row row) {
        // Check first few cells for duty section headers
        for (int i = 0; i < Math.min(5, row.getLastCellNum()); i++) {
            Cell cell = row.getCell(i);
            if (cell == null) continue;

            String value = getCellStringValue(cell).trim();
            if (value.isEmpty()) continue;

            // Look for duty section markers
            if (value.contains("Day Duty") || value.equals("Day Duty") ||
                    value.contains("Night Duty") || value.equals("Night Duty") ||
                    value.contains("Morning Duty") || value.equals("Morning Duty") ||
                    value.contains("Evening Duty") || value.equals("Evening Duty")) {
                return value;
            }
        }
        return null;
    }

    // Extract employee data from a row
    private EmployeeData extractEmployeeData(Row row, Map<String, Integer> headerMap) {
        // Get name column index
        Integer nameCol = headerMap.getOrDefault("Name", 2); // Default to column C
        Cell nameCell = row.getCell(nameCol);

        if (nameCell == null) {
            return null;
        }

        String name = getCellStringValue(nameCell).trim();

        // Skip empty names or rows that look like headers
        if (name.isEmpty() || name.equalsIgnoreCase("Name") || name.contains("Team") ||
                name.contains("Day Duty") || name.contains("Night Duty")) {
            return null;
        }

        // Extract mobile number (if available)
        Integer mobileNoCol = headerMap.getOrDefault("Mobile No", 6);
        String mobileNo = getCellStringValue(row.getCell(mobileNoCol)).trim();

        // Extract employee ID / Sr. No
        Integer srNoCol = headerMap.getOrDefault("Sr. No", 8);
        String srNo = getCellStringValue(row.getCell(srNoCol)).trim();

        // Generate employee ID if not present
        if (srNo == null || srNo.isEmpty()) {
            srNo = "EMP-" + name.replaceAll("[^a-zA-Z0-9]", "");
        }

        // Extract code name (short name or initials)
        String codeName = extractCodeName(name);

        // Extract shift information
        int totalShift = getCellIntValue(row, headerMap.getOrDefault("Total Shift", 9), 0);
        int rotShift = getCellIntValue(row, headerMap.getOrDefault("ROT Shift", 10), 0);
        int offDay = getCellIntValue(row, headerMap.getOrDefault("Off Day", 11), 0);
        int dDuty = getCellIntValue(row, headerMap.getOrDefault("D Duty", 12), 0);

        log.debug("Extracted employee: {}. Sr.No: {}, Total Shift: {}, ROT Shift: {}, Off Day: {}, D Duty: {}",
                name, srNo, totalShift, rotShift, offDay, dDuty);

        return new EmployeeData(name, mobileNo, srNo, codeName, totalShift, rotShift, offDay, dDuty);
    }

    // Scan for employees in the sheet
    private List<EmployeeData> scanForEmployees(Sheet sheet, HeaderRowInfo headerInfo) {
        List<EmployeeData> employees = new ArrayList<>();

        // Determine start row for scanning
        int startRow = headerInfo.getRowIndex() > 0 ? headerInfo.getRowIndex() + 1 : 0;

        // Scan all rows after the header
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            EmployeeData empData = extractEmployeeData(row, headerInfo.getHeaderMap());
            if (empData != null) {
                employees.add(empData);
            }
        }

        return employees;
    }

    // Extract code name from full name
    private String extractCodeName(String fullName) {
        // Check if there's a short name in parentheses like "John Smith (JS)"
        Pattern pattern = Pattern.compile("\\s*\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(fullName);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Extract initials from name
        StringBuilder initials = new StringBuilder();
        String[] parts = fullName.split("\\s+");

        // Use first letter of each part of the name
        for (String part : parts) {
            if (!part.isEmpty() && !part.startsWith("(")) {
                initials.append(part.charAt(0));
                if (initials.length() >= 2) break;
            }
        }

        // If we couldn't get 2 initials, use the first 2 chars of the first name
        if (initials.length() < 2 && parts.length > 0 && parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        }

        return initials.toString().toUpperCase();
    }


    private String getOrCreateTeam(String teamName) {
        Optional<Team> existingTeam = teamRepository.findByName(teamName);
        if (existingTeam.isPresent()) {
            return existingTeam.get().getId();
        }

        String shortName;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(teamName);

        if (matcher.find()) {
            shortName = "T " + Integer.parseInt(matcher.group());
        } else {
            shortName = "T " + (teamRepository.count() + 1);
        }

        TeamDto teamDto = TeamDto.builder()
                .name(teamName)
                .shortName(shortName)
                .active(true)
                .build();

        return teamService.createTeam(teamDto).getId();
    }

    // Get or create employee in the database
    private String getOrCreateEmployee(String name, String employeeId, String mobileNo, String codeName, String teamId) {
        // Check for existing employee
        Optional<Employee> existingEmployee = employeeRepository.findByEmployeeId(employeeId);
        if (existingEmployee.isPresent()) {
            return existingEmployee.get().getId();
        }

        // Create a new employee
        EmployeeDto employeeDto = EmployeeDto.builder()
                .employeeId(employeeId)
                .name(name)
                .teamId(teamId)
                .mobileNo(mobileNo)
                .shortName(codeName)
                .active(true)
                .build();

        return employeeService.createEmployee(employeeDto).getId();
    }

    // Helper method to get string value from cell safely
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    // Handle both integer and floating point values
                    double numVal = cell.getNumericCellValue();
                    if (numVal == Math.floor(numVal)) {
                        // It's an integer
                        return String.valueOf((long) numVal);
                    } else {
                        // It's a float
                        return String.valueOf(numVal);
                    }
                case FORMULA:
                    try {
                        // Try to get the cached formula result
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        try {
                            // If that fails, try numeric result
                            return String.valueOf(cell.getNumericCellValue());
                        } catch (Exception e2) {
                            // If all else fails, return empty string
                            return "";
                        }
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Error getting string value from cell: {}", e.getMessage());
            return "";
        }
    }

    // Helper method to get integer value from cell safely
    private int getCellIntValue(Row row, int colIndex, int defaultValue) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return defaultValue;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) return defaultValue;

                    try {
                        // Try to parse as integer
                        return Integer.parseInt(strValue);
                    } catch (NumberFormatException e) {
                        // Try to handle decimal format
                        try {
                            return (int) Double.parseDouble(strValue);
                        } catch (NumberFormatException ex) {
                            return defaultValue;
                        }
                    }
                case FORMULA:
                    try {
                        return (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        return defaultValue;
                    }
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            log.warn("Error getting int value for cell at column {}: {}", colIndex, e.getMessage());
            return defaultValue;
        }
    }


    private static class MonthYearInfo {
        private int month;
        private int year;

        public MonthYearInfo() {
        }

        public MonthYearInfo(int month, int year) {
            this.month = month;
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }


    private static class HeaderRowInfo {
        private boolean found;
        private int rowIndex;
        private Map<String, Integer> headerMap;
        private int nameColumnIndex;

        public HeaderRowInfo() {
        }

        public HeaderRowInfo(boolean found, int rowIndex, Map<String, Integer> headerMap, int nameColumnIndex) {
            this.found = found;
            this.rowIndex = rowIndex;
            this.headerMap = headerMap;
            this.nameColumnIndex = nameColumnIndex;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public Map<String, Integer> getHeaderMap() {
            return headerMap;
        }

        public void setHeaderMap(Map<String, Integer> headerMap) {
            this.headerMap = headerMap;
        }

        public int getNameColumnIndex() {
            return nameColumnIndex;
        }

        public void setNameColumnIndex(int nameColumnIndex) {
            this.nameColumnIndex = nameColumnIndex;
        }
    }

    private static class EmployeeData {
        private String name;
        private String mobileNo;
        private String srNo;
        private String codeName;
        private int totalShift;
        private int rotShift;
        private int offDay;
        private int dDuty;

        public EmployeeData() {
        }

        public EmployeeData(String name, String mobileNo, String srNo, String codeName, int totalShift, int rotShift, int offDay, int dDuty) {
            this.name = name;
            this.mobileNo = mobileNo;
            this.srNo = srNo;
            this.codeName = codeName;
            this.totalShift = totalShift;
            this.rotShift = rotShift;
            this.offDay = offDay;
            this.dDuty = dDuty;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getSrNo() {
            return srNo;
        }

        public void setSrNo(String srNo) {
            this.srNo = srNo;
        }

        public String getCodeName() {
            return codeName;
        }

        public void setCodeName(String codeName) {
            this.codeName = codeName;
        }

        public int getTotalShift() {
            return totalShift;
        }

        public void setTotalShift(int totalShift) {
            this.totalShift = totalShift;
        }

        public int getRotShift() {
            return rotShift;
        }

        public void setRotShift(int rotShift) {
            this.rotShift = rotShift;
        }

        public int getOffDay() {
            return offDay;
        }

        public void setOffDay(int offDay) {
            this.offDay = offDay;
        }

        public int getdDuty() {
            return dDuty;
        }

        public void setdDuty(int dDuty) {
            this.dDuty = dDuty;
        }
    }
}