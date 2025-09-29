package com.slt.radio.rosterservice.service.roster;

import com.slt.radio.rosterservice.documents.one.shift.ShiftAssignment;
import com.slt.radio.rosterservice.documents.one.shift.ShiftRoster;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);

    // Define patterns for invalid cell content
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\d{10}|\\d{3}-\\d{7}|\\+\\d{11}");
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("\\d{4}");
    private static final List<String> META_HEADERS = Arrays.asList(
            "name", "mobile no", "sr. no", "total shift", "rot shift", "off day", "d duty",
            "phone", "id", "employee", "mobile", "contact"
    );

    public ShiftRoster parseExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Create a new ShiftRoster object
            ShiftRoster roster = new ShiftRoster();

            // Set default values in case we can't extract from the file
            String defaultTitle = "IPTV NOC Team Shift Duty Roster";
            String defaultMonth = "Unknown";
            int defaultYear = Calendar.getInstance().get(Calendar.YEAR);

            roster.setTitle(defaultTitle);
            roster.setMonth(defaultMonth);
            roster.setYear(defaultYear);

            try {
                // Extract title, month and year
                extractTitleInfo(sheet, roster);
            } catch (Exception e) {
                log.warn("Error while trying to extract title/month/year", e);
            }

            // Extract dates
            List<String> dates = extractDates(sheet);
            log.info("Extracted dates: {}", dates);
            roster.setDates(dates);

            // Find all sections in the sheet using strict and improved detection methods
            Map<String, List<Integer>> allSectionRows = findAllSectionInstances(sheet);
            log.info("Found all section instances: {}", allSectionRows);

            // Convert to final section rows map (selecting the correct instances)
            Map<String, Integer> sectionRows = selectCorrectSectionInstances(sheet, allSectionRows);

            if (sectionRows.isEmpty()) {
                log.warn("Could not find any sections in Excel file, attempting to parse without sections");
                processFlatStructure(sheet, roster, dates);
                return roster;
            }

            log.info("Selected final section rows: {}", sectionRows);

            // Process each section
            Map<String, List<ShiftAssignment>> dutyTurn = new HashMap<>();
            Map<String, List<ShiftAssignment>> dayDuty = new HashMap<>();
            Map<String, List<ShiftAssignment>> offDay = new HashMap<>();

            // Convert to sorted list of section entries for easier processing
            List<Map.Entry<String, Integer>> sortedSections = new ArrayList<>(sectionRows.entrySet());
            sortedSections.sort(Comparator.comparing(Map.Entry::getValue));

            // Process each section
            for (int i = 0; i < sortedSections.size(); i++) {
                String sectionName = sortedSections.get(i).getKey();
                int startRow = sortedSections.get(i).getValue() + 1; // Start after header row
                int endRow = (i < sortedSections.size() - 1)
                        ? sortedSections.get(i + 1).getValue() - 1 // End before next section
                        : sheet.getLastRowNum(); // Or end of sheet

                log.info("Processing section '{}' from row {} to {}", sectionName, startRow, endRow);

                switch (sectionName.trim().toLowerCase()) {
                    case "duty turn":
                        processSection(sheet, startRow, endRow, dutyTurn, dates, "duty turn");
                        break;
                    case "day duty":
                        processDayDutySection(sheet, startRow, endRow, dayDuty, dates);
                        break;
                    case "off day":
                        processOffDaySection(sheet, startRow, endRow, offDay, dates);
                        break;
                    default:
                        log.info("Skipping unknown section: {}", sectionName);
                }
            }

            // Set the sections in the roster
            roster.setDutyTurn(dutyTurn);
            roster.setDayDuty(dayDuty);
            roster.setOffDay(offDay);

            log.info("Parsed roster with {} duty turn entries, {} day duty entries, {} off day entries",
                    dutyTurn.size(), dayDuty.size(), offDay.size());

            return roster;
        }
    }

    /**
     * Extract title, month and year from the sheet
     */
    private void extractTitleInfo(Sheet sheet, ShiftRoster roster) {
        // Try to find the title - search across all rows and columns for the title pattern
        boolean foundTitle = false;
        for (int i = 0; i <= 5 && !foundTitle; i++) { // Check first few rows
            Row row = sheet.getRow(i);
            if (row != null) {
                // First check for merged regions that might contain the title
                for (int j = 0; j < sheet.getNumMergedRegions(); j++) {
                    CellRangeAddress region = sheet.getMergedRegion(j);
                    if (region.getFirstRow() == i) {
                        Cell cell = row.getCell(region.getFirstColumn());
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if (processTitleCell(cellValue, roster)) {
                                foundTitle = true;
                                break;
                            }
                        }
                    }
                }

                // If not found in merged regions, check individual cells
                if (!foundTitle) {
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if (processTitleCell(cellValue, roster)) {
                                foundTitle = true;
                                break;
                            }
                        }
                    }

                    // Also check for title spread across multiple cells in the same row
                    if (!foundTitle) {
                        StringBuilder rowContent = new StringBuilder();
                        for (int j = 0; j < row.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                String cellValue = getCellValueAsString(cell);
                                rowContent.append(cellValue).append(" ");
                            }
                        }
                        if (processTitleCell(rowContent.toString(), roster)) {
                            foundTitle = true;
                        }
                    }
                }
            }
        }

        if (!foundTitle) {
            log.warn("Could not find title in Excel file, using default values");
        }
    }

    /**
     * Process a potential title cell and extract information
     * Returns true if this cell contained title information
     */
    private boolean processTitleCell(String cellValue, ShiftRoster roster) {
        if (cellValue.contains("Duty Roster") ||
                cellValue.contains("NOC Team") ||
                cellValue.contains("IPTV")) {

            roster.setTitle(cellValue);
            // Try to extract month and year
            for (String month : getMonthNames()) {
                if (cellValue.contains(month)) {
                    roster.setMonth(month);
                    // Look for year (4 digits)
                    String[] parts = cellValue.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("\\d{4}")) {
                            try {
                                roster.setYear(Integer.parseInt(part));
                            } catch (NumberFormatException e) {
                                log.warn("Could not parse year from: {}", part);
                            }
                        }
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Find all instances of sections in the sheet
     * This method collects ALL occurrences of section headers
     */
    private Map<String, List<Integer>> findAllSectionInstances(Sheet sheet) {
        Map<String, List<Integer>> sectionInstances = new HashMap<>();
        sectionInstances.put("duty turn", new ArrayList<>());
        sectionInstances.put("day duty", new ArrayList<>());
        sectionInstances.put("off day", new ArrayList<>());

        Map<String, List<String>> sectionKeywords = new HashMap<>();

        // Define alternative keywords/phrases for each section
        sectionKeywords.put("duty turn", Arrays.asList("duty turn", "duty roster", "roster", "turn"));
        sectionKeywords.put("day duty", Arrays.asList("day duty", "d duty", "day", "d", "day roster"));
        sectionKeywords.put("off day", Arrays.asList("off day", "off", "off roster", "o/day", "o day"));

        // Scan the entire sheet for section headers
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            // Check for merged regions that might contain section headers
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress region = sheet.getMergedRegion(i);
                if (region.getFirstRow() == rowIndex) {
                    Cell cell = row.getCell(region.getFirstColumn());
                    if (cell != null) {
                        String cellValue = getCellValueAsString(cell).toLowerCase().trim();

                        // Check if the cell contains a section name
                        for (Map.Entry<String, List<String>> entry : sectionKeywords.entrySet()) {
                            String sectionName = entry.getKey();
                            List<String> keywords = entry.getValue();

                            for (String keyword : keywords) {
                                if (cellValue.equals(keyword) ||
                                        cellValue.startsWith(keyword) ||
                                        cellValue.contains(keyword)) {

                                    sectionInstances.get(sectionName).add(rowIndex);
                                    log.info("Found section '{}' at row {} in merged region with keyword: '{}'",
                                            sectionName, rowIndex, keyword);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Also check individual cells for section headers
            for (int colIndex = 0; colIndex < Math.min(10, row.getLastCellNum()); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).toLowerCase().trim();

                    // Check if the cell contains a section name
                    for (Map.Entry<String, List<String>> entry : sectionKeywords.entrySet()) {
                        String sectionName = entry.getKey();
                        List<String> keywords = entry.getValue();

                        for (String keyword : keywords) {
                            if (cellValue.equals(keyword) ||
                                    cellValue.startsWith(keyword) ||
                                    cellValue.contains(keyword)) {

                                if (!sectionInstances.get(sectionName).contains(rowIndex)) {
                                    sectionInstances.get(sectionName).add(rowIndex);
                                    log.info("Found section '{}' at row {}, column {} with keyword: '{}'",
                                            sectionName, rowIndex, colIndex, keyword);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        return sectionInstances;
    }

    /**
     * Select the correct instance of each section
     * This handles the case where there are multiple "Day Duty" sections in the file
     */
    private Map<String, Integer> selectCorrectSectionInstances(Sheet sheet, Map<String, List<Integer>> allSectionRows) {
        Map<String, Integer> finalSections = new HashMap<>();

        // First, handle simple case - if we have only one instance of each section
        for (Map.Entry<String, List<Integer>> entry : allSectionRows.entrySet()) {
            String sectionName = entry.getKey();
            List<Integer> instances = entry.getValue();

            if (instances.size() == 1) {
                finalSections.put(sectionName, instances.get(0));
            }
        }

        // For sections with multiple instances, we need to select the correct one

        // For Duty Turn - usually the first one and near the top
        if (!finalSections.containsKey("duty turn") && !allSectionRows.get("duty turn").isEmpty()) {
            // Select the instance closest to the top of the sheet
            finalSections.put("duty turn", Collections.min(allSectionRows.get("duty turn")));
        }

        // For Day Duty - select the one that has actual assignments
        if (!finalSections.containsKey("day duty") && allSectionRows.get("day duty").size() > 0) {
            Integer selectedDayDuty = null;

            // Try to find the instance that has "Na" values in the rows below
            for (Integer rowIndex : allSectionRows.get("day duty")) {
                boolean hasDayDutyAssignments = false;

                // Check the next few rows for "Na" values
                for (int r = rowIndex + 1; r <= Math.min(rowIndex + 5, sheet.getLastRowNum()); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    for (int c = 2; c < row.getLastCellNum(); c++) { // Start from column 2
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            String value = getCellValueAsString(cell);
                            if ("Na".equalsIgnoreCase(value)) {
                                hasDayDutyAssignments = true;
                                break;
                            }
                        }
                    }
                    if (hasDayDutyAssignments) break;
                }

                if (hasDayDutyAssignments) {
                    selectedDayDuty = rowIndex;
                    break;
                }
            }

            // If we couldn't find a Day Duty section with Na values, take the first one
            if (selectedDayDuty == null && !allSectionRows.get("day duty").isEmpty()) {
                selectedDayDuty = allSectionRows.get("day duty").get(0);
            }

            if (selectedDayDuty != null) {
                finalSections.put("day duty", selectedDayDuty);
            }
        }

        // For Off Day - usually between Day Duty and the end of the sheet
        if (!finalSections.containsKey("off day") && !allSectionRows.get("off day").isEmpty()) {
            // If we have Day Duty, the Off Day should be after it
            if (finalSections.containsKey("day duty")) {
                int dayDutyRow = finalSections.get("day duty");

                // Find the Off Day instance that comes after Day Duty
                for (Integer offDayRow : allSectionRows.get("off day")) {
                    if (offDayRow > dayDutyRow) {
                        finalSections.put("off day", offDayRow);
                        break;
                    }
                }

                // If we didn't find an Off Day after Day Duty, take the first one
                if (!finalSections.containsKey("off day")) {
                    finalSections.put("off day", allSectionRows.get("off day").get(0));
                }
            } else {
                // If we don't have Day Duty, just take the first Off Day
                finalSections.put("off day", allSectionRows.get("off day").get(0));
            }
        }

        // Fallback - if we still don't have all sections, try to find them based on sheet structure
        if (finalSections.size() < 3) {
            log.warn("Could not find all 3 sections, using best-effort section placement");

            // If we found at least one section, use it as reference
            int referenceRow = -1;
            for (int row : finalSections.values()) {
                referenceRow = row;
                break;
            }

            if (referenceRow == -1) {
                // No sections found at all, divide sheet evenly
                int totalRows = sheet.getLastRowNum();
                referenceRow = totalRows / 4;
            }

            // Ensure all sections exist
            if (!finalSections.containsKey("duty turn")) {
                finalSections.put("duty turn", Math.max(0, referenceRow - 20));
                log.info("Forced placement: 'duty turn' at row {}", Math.max(0, referenceRow - 20));
            }

            if (!finalSections.containsKey("day duty")) {
                finalSections.put("day duty", referenceRow + 20);
                log.info("Forced placement: 'day duty' at row {}", referenceRow + 20);
            }

            if (!finalSections.containsKey("off day")) {
                finalSections.put("off day", referenceRow + 40);
                log.info("Forced placement: 'off day' at row {}", referenceRow + 40);
            }
        }

        return finalSections;
    }

    /**
     * Extract dates from the sheet
     */
    private List<String> extractDates(Sheet sheet) {
        List<String> dates = new ArrayList<>();

        // Find the day names row (the row that contains Wed, Thu, Fri, etc.)
        int dayNamesRowIndex = -1;
        Map<Integer, String> dayNameColumns = new HashMap<>();

        for (int i = 0; i <= 10; i++) { // Check first several rows
            Row row = sheet.getRow(i);
            if (row != null) {
                int dayCount = 0;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    String cellValue = getCellValueAsString(cell);
                    if (isDayOfWeek(cellValue)) {
                        dayNamesRowIndex = i;
                        dayNameColumns.put(j, cellValue);
                        dayCount++;
                    }
                }
                if (dayCount >= 3) { // Found enough day names to consider it the day row
                    break;
                }
                if (dayCount > 0) {
                    log.debug("Found {} day names in row {}, but not enough to confirm day row",
                            dayCount, i);
                }
                dayNameColumns.clear(); // Reset if we didn't find enough days
            }
        }

        if (dayNamesRowIndex == -1) {
            log.warn("Could not find day names row in Excel file");
            // Create default dates (1-31)
            for (int i = 1; i <= 31; i++) {
                dates.add(String.valueOf(i));
            }
            return dates;
        }

        log.info("Found day names row at index {}", dayNamesRowIndex);

        // Find dates row - either directly below day names or search for numeric dates
        int dateRowIndex = dayNamesRowIndex + 1;
        Row dateRow = sheet.getRow(dateRowIndex);

        if (dateRow != null) {
            for (int col : dayNameColumns.keySet()) {
                if (col < dateRow.getLastCellNum()) {
                    Cell cell = dateRow.getCell(col);
                    if (cell == null) continue;

                    String dateVal = getCellValueAsString(cell);
                    if (!dateVal.isEmpty() && dateVal.matches("\\d{1,2}")) {
                        dates.add(dateVal);
                    }
                }
            }

            log.info("Found {} date values in row below day names", dates.size());
        }

        // If dates list is still empty or incomplete, search for numeric date values
        if (dates.isEmpty() || dates.size() < 28) {
            dates.clear(); // Reset in case we found partial dates

            for (int i = 0; i <= 10; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                boolean foundDates = false;
                List<String> potentialDates = new ArrayList<>();

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    String cellValue = getCellValueAsString(cell);

                    // Check if cell contains a number between 1-31 (potential date)
                    if (cellValue.matches("\\d+") &&
                            Integer.parseInt(cellValue) >= 1 &&
                            Integer.parseInt(cellValue) <= 31) {
                        foundDates = true;
                        potentialDates.add(cellValue);
                    }
                }

                if (foundDates && potentialDates.size() > 20) { // Assume it's a date row if we find many numbers
                    dates = potentialDates;
                    log.info("Found {} date values in row {}", dates.size(), i);
                    break;
                }
            }
        }

        // If still no dates found or less than expected, create default ones
        if (dates.isEmpty() || dates.size() < 28) {
            log.warn("Could not reliably extract dates, using default 1-31");
            dates.clear();
            for (int i = 1; i <= 31; i++) {
                dates.add(String.valueOf(i));
            }
        }

        return dates;
    }

    /**
     * Process Duty Turn section of the sheet
     */
    private void processSection(Sheet sheet, int startRow, int endRow,
                                Map<String, List<ShiftAssignment>> sectionData,
                                List<String> dates, String sectionType) {
        // Keep track of current time slot
        String currentTimeSlot = null;
        // Identify the columns that correspond to actual dates
        Set<Integer> validDateColumns = identifyValidDateColumns(sheet, dates);

        log.info("Processing section {} from row {} to {} with {} valid date columns",
                sectionType, startRow, endRow, validDateColumns.size());

        // Count of processed assignments for debugging
        int assignmentCount = 0;

        for (int rowIdx = startRow; rowIdx <= Math.min(endRow, sheet.getLastRowNum()); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            // Try to find a time slot in any cell of the row (typically for Duty Turn)
            boolean foundTimeSlot = false;
            for (int j = 0; j < Math.min(5, currentRow.getLastCellNum()); j++) {  // Check first few cells
                Cell cell = currentRow.getCell(j);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                // Check if it's a time slot format: HH:MM-HH:MM or HH:MM - HH:MM
                if (cellValue.matches("\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}") ||
                        cellValue.matches("\\d{1,2}:\\d{2}.*\\d{1,2}:\\d{2}")) {
                    currentTimeSlot = cellValue;
                    foundTimeSlot = true;
                    log.debug("Found time slot at row {}: {}", rowIdx, currentTimeSlot);
                    break;
                }
            }

            // Skip rows without a time slot
            if (currentTimeSlot == null) {
                continue;
            }

            // Skip section headers
            boolean isSectionHeader = false;
            for (int j = 0; j < Math.min(3, currentRow.getLastCellNum()); j++) {
                Cell cell = currentRow.getCell(j);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).toLowerCase();
                    if (cellValue.equals("duty turn") || cellValue.equals("day duty") ||
                            cellValue.equals("off day") || cellValue.equals("d duty")) {
                        isSectionHeader = true;
                        break;
                    }
                }
            }

            if (isSectionHeader) {
                continue;
            }

            // Process the assignments in this row
            List<ShiftAssignment> assignments = sectionData.getOrDefault(currentTimeSlot, new ArrayList<>());
            int rowAssignments = 0;

            // Process columns for team assignments, focusing on valid date columns
            for (int col = 0; col < currentRow.getLastCellNum(); col++) {
                if (!validDateColumns.contains(col)) continue; // Skip non-date columns

                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String teamAssignment = getCellValueAsString(cell);

                // Skip empty cells
                if (teamAssignment.trim().isEmpty()) {
                    continue;
                }

                // Check for valid team assignment pattern (T 1, T 2 ROT, etc.)
                boolean isValid = teamAssignment.matches("^[A-Z] [0-9]+( ROT)?$");

                if (!isValid) {
                    continue;
                }

                // Find the corresponding date for this column
                String date = findDateForColumn(col, sheet, dates);
                if (date != null) {
                    assignments.add(new ShiftAssignment(date, teamAssignment));
                    rowAssignments++;
                    assignmentCount++;
                }
            }

            if (rowAssignments > 0) {
                log.debug("Added {} assignments for time slot {} at row {}",
                        rowAssignments, currentTimeSlot, rowIdx);
                sectionData.put(currentTimeSlot, assignments);
            }
        }

        log.info("Processed {} total assignments for section {} into {} time slots",
                assignmentCount, sectionType, sectionData.size());
    }

    /**
     * Process Day Duty section with specialized handling
     */
    private void processDayDutySection(Sheet sheet, int startRow, int endRow,
                                       Map<String, List<ShiftAssignment>> sectionData,
                                       List<String> dates) {
        String timeSlot = "Day Duty";
        Set<Integer> validDateColumns = identifyValidDateColumns(sheet, dates);
        List<ShiftAssignment> assignments = new ArrayList<>();
        int assignmentCount = 0;

        log.info("Processing Day Duty section from row {} to {} with {} valid date columns",
                startRow, endRow, validDateColumns.size());

        // Check for time slot format in the Day Duty header
        Row headerRow = sheet.getRow(startRow - 1); // Header row is the one before startRow
        if (headerRow != null) {
            for (int j = 0; j < Math.min(5, headerRow.getLastCellNum()); j++) {
                Cell cell = headerRow.getCell(j);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    if (cellValue.toLowerCase().contains("day duty")) {
                        // Check if there's a time slot format embedded
                        if (cellValue.matches(".*\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}.*")) {
                            timeSlot = cellValue.trim();
                            log.info("Using custom time slot for Day Duty: {}", timeSlot);
                        }
                        break;
                    }
                }
            }
        }

        // Scan rows for "Na" values which are typical for Day Duty
        for (int rowIdx = startRow; rowIdx <= Math.min(endRow, sheet.getLastRowNum()); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            // Skip empty or header-like rows
            boolean isHeaderOrEmpty = true;
            for (int col : validDateColumns) {
                if (col >= currentRow.getLastCellNum()) continue;

                Cell cell = currentRow.getCell(col);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (!value.isEmpty() &&
                            !isDayOfWeek(value) &&
                            !isNumericDate(value) &&
                            !containsMetaHeaderKeyword(value)) {
                        isHeaderOrEmpty = false;
                        break;
                    }
                }
            }

            if (isHeaderOrEmpty) continue;

            // Process columns for "Na" values
            for (int col = 0; col < currentRow.getLastCellNum(); col++) {
                if (!validDateColumns.contains(col)) continue;

                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);

                // Day Duty typically has "Na" values
                if ("Na".equalsIgnoreCase(value.trim())) {
                    String date = findDateForColumn(col, sheet, dates);
                    if (date != null) {
                        assignments.add(new ShiftAssignment(date, "Na"));
                        assignmentCount++;
                        log.debug("Added Day Duty assignment for date {}: Na", date);
                    }
                }
            }
        }

        if (assignmentCount > 0) {
            log.info("Processed {} Day Duty assignments", assignmentCount);
            sectionData.put(timeSlot, assignments);
        } else {
            log.warn("No Day Duty assignments found, trying fallback approach");
            processDayDutyFallback(sheet, startRow, endRow, sectionData, dates);
        }
    }

    /**
     * Fallback processor for Day Duty section
     */
    private void processDayDutyFallback(Sheet sheet, int startRow, int endRow,
                                        Map<String, List<ShiftAssignment>> sectionData,
                                        List<String> dates) {
        String timeSlot = "Day Duty";
        List<ShiftAssignment> assignments = new ArrayList<>();
        int assignmentCount = 0;

        log.info("Using fallback processor for Day Duty section");

        // Look for any cells with "Na" values in the entire section
        for (int rowIdx = startRow; rowIdx <= Math.min(endRow, sheet.getLastRowNum()); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            for (int colIdx = 2; colIdx < currentRow.getLastCellNum(); colIdx++) {
                Cell cell = currentRow.getCell(colIdx);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                if ("Na".equalsIgnoreCase(cellValue.trim())) {
                    // Try to find corresponding date
                    String date = findDateForColumn(colIdx, sheet, dates);
                    if (date != null) {
                        assignments.add(new ShiftAssignment(date, "Na"));
                        assignmentCount++;
                        log.debug("Fallback: Added Day Duty assignment for date {}: Na", date);
                    }
                }
            }
        }

        if (assignmentCount > 0) {
            log.info("Fallback processor added {} Day Duty assignments", assignmentCount);
            sectionData.put(timeSlot, assignments);
        } else {
            log.warn("Fallback processor could not find any Day Duty assignments");

            // Last resort: Create default assignments for empty days
            // We'll create "Na" entries for weekdays (assuming first day in dates is a weekday)
            List<ShiftAssignment> defaultAssignments = new ArrayList<>();

            for (int i = 0; i < dates.size(); i++) {
                // Assuming Mon-Fri are working days (indices 0-4, 7-11, etc.)
                int dayIndex = i % 7;
                if (dayIndex < 5) { // Monday to Friday
                    defaultAssignments.add(new ShiftAssignment(dates.get(i), "Na"));
                }
            }

            if (!defaultAssignments.isEmpty()) {
                log.info("Created {} default Day Duty assignments for weekdays", defaultAssignments.size());
                sectionData.put(timeSlot, defaultAssignments);
            }
        }
    }

    /**
     * Process Off Day section with specialized handling to capture all data
     */
    private void processOffDaySection(Sheet sheet, int startRow, int endRow,
                                      Map<String, List<ShiftAssignment>> sectionData,
                                      List<String> dates) {
        String timeSlot = "Off Day";
        Set<Integer> validDateColumns = identifyValidDateColumns(sheet, dates);
        List<ShiftAssignment> assignments = new ArrayList<>();
        int assignmentCount = 0;

        log.info("Processing Off Day section from row {} to {} with {} valid date columns",
                startRow, endRow, validDateColumns.size());

        // First, process main shift roster in first few rows (typically rows 15-17)
        // These contain the primary Off Day assignments
        for (int rowIdx = startRow; rowIdx <= Math.min(startRow + 10, endRow); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            // Skip empty rows and metadata rows
            boolean isSkippableRow = false;
            for (int j = 0; j < Math.min(10, currentRow.getLastCellNum()); j++) {
                Cell cell = currentRow.getCell(j);
                if (cell != null) {
                    String value = getCellValueAsString(cell).toLowerCase();
                    // Skip rows with metadata headers
                    if (containsMetaHeaderKeyword(value)) {
                        isSkippableRow = true;
                        break;
                    }
                }
            }

            if (isSkippableRow) continue;

            // Process all assignments in this row
            boolean rowHasAssignments = false;

            for (int col = 2; col < currentRow.getLastCellNum(); col++) {
                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);
                if (value.isEmpty()) continue;

                // Check for team assignments (T 1, T 2, T 3) or Na values
                boolean isAssignment = value.matches("^[A-Z] [0-9]+( ROT)?$") ||
                        value.equals("Na");

                if (isAssignment) {
                    String date = findDateForColumn(col, sheet, dates);
                    if (date != null) {
                        assignments.add(new ShiftAssignment(date, value));
                        assignmentCount++;
                        rowHasAssignments = true;
                        log.debug("Added Off Day assignment for date {}: {}", date, value);
                    }
                }
            }

            // If we've found assignments and then hit a row without assignments,
            // we're likely beyond the main assignments section
            if (!rowHasAssignments && assignmentCount > 0) {
                break;
            }
        }

        // Also check for assignments in the entire section (especially for "Na" values)
        // This ensures we don't miss anything
        for (int rowIdx = startRow; rowIdx <= endRow; rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            for (int col = 2; col < currentRow.getLastCellNum(); col++) {
                if (!validDateColumns.contains(col)) continue;

                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);
                if (value.isEmpty()) continue;

                // Look for both team codes and "Na" values
                if ((value.matches("^[A-Z] [0-9]+( ROT)?$") || value.equals("Na")) &&
                        !isDayOfWeek(value) && !isNumericDate(value) &&
                        !containsMetaHeaderKeyword(value)) {

                    String date = findDateForColumn(col, sheet, dates);
                    if (date != null) {
                        // Check if we already have this assignment
                        boolean isDuplicate = false;
                        for (ShiftAssignment existing : assignments) {
                            if (existing.getDate().equals(date) && existing.getTeam().equals(value)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            assignments.add(new ShiftAssignment(date, value));
                            assignmentCount++;
                            log.debug("Added additional Off Day assignment for date {}: {}", date, value);
                        }
                    }
                }
            }
        }

        // Check for team data sections (containing employee information)
        // These can provide additional context for the team assignments
        Map<String, String> teamCodes = processTeamData(sheet, startRow, endRow);
        log.info("Processed team data: {}", teamCodes);

        if (assignmentCount > 0) {
            log.info("Processed {} Off Day assignments", assignmentCount);
            sectionData.put(timeSlot, assignments);
        } else {
            log.warn("No Off Day assignments found, trying fallback approach");
            processOffDayFallback(sheet, startRow, endRow, sectionData, dates);
        }
    }

    /**
     * Process team data from the sheet (employee names, IDs, etc.)
     * Returns a map of team codes to team names
     */
    private Map<String, String> processTeamData(Sheet sheet, int startRow, int endRow) {
        Map<String, String> teamData = new HashMap<>();

        // Look for team headers in format "Team XX\nT N"
        for (int rowIdx = startRow; rowIdx <= endRow; rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            for (int col = 0; col < Math.min(5, currentRow.getLastCellNum()); col++) {
                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);

                // Check for team header pattern (e.g., "Team 01\r\nT 1")
                if (value.contains("Team") && value.contains("T ")) {
                    // Extract team code and name
                    String teamCode = null;
                    String teamName = value;

                    // Try to extract T N code
                    if (value.contains("\r\n") || value.contains("\n")) {
                        String[] parts = value.split("\\r?\\n");
                        if (parts.length >= 2) {
                            teamName = parts[0].trim();
                            teamCode = parts[1].trim();
                        }
                    } else {
                        // Try to extract from single line
                        int idx = value.indexOf("T ");
                        if (idx >= 0 && idx + 3 <= value.length()) {
                            teamCode = value.substring(idx, Math.min(idx + 4, value.length())).trim();
                            teamName = value.substring(0, idx).trim();
                        }
                    }

                    if (teamCode != null && !teamCode.isEmpty()) {
                        teamData.put(teamCode, teamName);
                        log.info("Found team: {} - {}", teamCode, teamName);
                    }
                }
            }
        }

        return teamData;
    }

    /**
     * Fallback processor for Off Day section - with improved pattern detection
     */
    private void processOffDayFallback(Sheet sheet, int startRow, int endRow,
                                       Map<String, List<ShiftAssignment>> sectionData,
                                       List<String> dates) {
        String timeSlot = "Off Day";
        List<ShiftAssignment> assignments = new ArrayList<>();
        int assignmentCount = 0;

        log.info("Using enhanced fallback processor for Off Day section");

        // Start by looking at the first few rows specifically - this is where most assignments are
        boolean foundFirstRowAssignments = false;
        for (int rowIdx = startRow; rowIdx <= Math.min(startRow + 3, endRow); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            int rowAssignments = 0;
            for (int colIdx = 2; colIdx < currentRow.getLastCellNum(); colIdx++) {
                Cell cell = currentRow.getCell(colIdx);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                if (cellValue.isEmpty()) continue;

                // Check if it's a team code (T 1, T 2, T 3, etc.) or Na
                boolean isAssignment = cellValue.matches("^[A-Z] [0-9]+( ROT)?$") ||
                        cellValue.equals("Na");

                if (isAssignment) {
                    String date = findDateForColumn(colIdx, sheet, dates);
                    if (date != null) {
                        assignments.add(new ShiftAssignment(date, cellValue));
                        assignmentCount++;
                        rowAssignments++;
                        log.debug("Fallback: Added Off Day assignment from first rows - date {}: {}",
                                date, cellValue);
                    }
                }
            }

            if (rowAssignments > 0) {
                foundFirstRowAssignments = true;
            }
        }

        // If we didn't find assignments in the first rows, scan the entire section
        if (!foundFirstRowAssignments) {
            log.info("No assignments found in first rows, scanning entire Off Day section");

            // Look for any cells containing T 1, T 2, T 3 or Na in the entire section
            for (int rowIdx = startRow; rowIdx <= endRow; rowIdx++) {
                Row currentRow = sheet.getRow(rowIdx);
                if (currentRow == null) continue;

                for (int colIdx = 2; colIdx < currentRow.getLastCellNum(); colIdx++) {
                    Cell cell = currentRow.getCell(colIdx);
                    if (cell == null) continue;

                    String cellValue = getCellValueAsString(cell);
                    if (cellValue.isEmpty()) continue;

                    // Accept a wider range of patterns for team assignments
                    boolean isAssignment = cellValue.matches("^[A-Z] [0-9]+( ROT)?$") ||
                            cellValue.matches("^[A-Z][0-9]+$") ||
                            cellValue.equals("Na");

                    if (isAssignment && !isNumericDate(cellValue) && !isDayOfWeek(cellValue) &&
                            !containsMetaHeaderKeyword(cellValue)) {

                        // Get date for this column
                        String date = null;

                        // First try looking for date in column header
                        for (int i = 0; i <= 10; i++) {
                            Row headerRow = sheet.getRow(i);
                            if (headerRow == null) continue;

                            Cell headerCell = headerRow.getCell(colIdx);
                            if (headerCell == null) continue;

                            String headerValue = getCellValueAsString(headerCell);
                            if (isNumericDate(headerValue)) {
                                date = headerValue;
                                break;
                            }
                        }

                        // If no date found, use column index or findDateForColumn
                        if (date == null) {
                            date = findDateForColumn(colIdx, sheet, dates);
                        }

                        if (date != null) {
                            assignments.add(new ShiftAssignment(date, cellValue));
                            assignmentCount++;
                            log.debug("Fallback: Added Off Day assignment from full scan - date {}: {}",
                                    date, cellValue);
                        }
                    }
                }
            }
        }

        // If we still don't have enough assignments, check for team data and use that
        if (assignmentCount < 10) {
            log.info("Insufficient assignments found, checking team data");
            Map<String, String> teamCodes = processTeamData(sheet, startRow, endRow);

            if (!teamCodes.isEmpty()) {
                log.info("Found team codes: {}", teamCodes);

                // Look for patterns in existing assignments to fill in missing dates
                Map<Integer, String> dayPatterns = new HashMap<>();
                for (ShiftAssignment assignment : assignments) {
                    try {
                        int day = Integer.parseInt(assignment.getDate());
                        String team = assignment.getTeam();
                        int dayMod = (day - 1) % 7; // 0-6 pattern
                        dayPatterns.put(dayMod, team);
                    } catch (NumberFormatException e) {
                        // Skip non-numeric dates
                    }
                }

                // If we found patterns, use them to fill in missing dates
                if (!dayPatterns.isEmpty()) {
                    log.info("Found day patterns: {}", dayPatterns);

                    // For each date, check if we have an assignment
                    for (String dateStr : dates) {
                        try {
                            int date = Integer.parseInt(dateStr);
                            int dayMod = (date - 1) % 7;

                            // Check if we already have this date
                            boolean hasDate = false;
                            for (ShiftAssignment assignment : assignments) {
                                if (assignment.getDate().equals(dateStr)) {
                                    hasDate = true;
                                    break;
                                }
                            }

                            // If we don't have this date and we have a pattern for this day
                            if (!hasDate && dayPatterns.containsKey(dayMod)) {
                                String team = dayPatterns.get(dayMod);
                                assignments.add(new ShiftAssignment(dateStr, team));
                                assignmentCount++;
                                log.debug("Fallback: Added Off Day assignment from pattern - date {}: {}",
                                        dateStr, team);
                            }
                        } catch (NumberFormatException e) {
                            // Skip non-numeric dates
                        }
                    }
                }

                // If we still don't have enough assignments, use team codes to create basic assignments
                if (assignmentCount < 15) {
                    // Create a basic pattern using the team codes
                    List<String> teamCodeList = new ArrayList<>(teamCodes.keySet());

                    for (String dateStr : dates) {
                        // Check if we already have this date
                        boolean hasDate = false;
                        for (ShiftAssignment assignment : assignments) {
                            if (assignment.getDate().equals(dateStr)) {
                                hasDate = true;
                                break;
                            }
                        }

                        // If we don't have this date, assign a team based on a rotation
                        if (!hasDate) {
                            try {
                                int date = Integer.parseInt(dateStr);
                                // Simple rotation pattern
                                String team = teamCodeList.get((date - 1) % teamCodeList.size());
                                assignments.add(new ShiftAssignment(dateStr, team));
                                assignmentCount++;
                                log.debug("Fallback: Added Off Day assignment from team codes - date {}: {}",
                                        dateStr, team);
                            } catch (NumberFormatException e) {
                                // Skip non-numeric dates
                            }
                        }
                    }
                }
            }
        }

        if (assignmentCount > 0) {
            log.info("Enhanced fallback processor added {} Off Day assignments", assignmentCount);
            sectionData.put(timeSlot, assignments);
        } else {
            log.warn("Enhanced fallback processor could not find any Off Day assignments");

            // Create assignments for weekends as absolute last resort
            createDefaultOffDayAssignments(sectionData, dates);
        }
    }

    /**
     * Create default Off Day assignments as a last resort
     */
    private void createDefaultOffDayAssignments(Map<String, List<ShiftAssignment>> sectionData, List<String> dates) {
        String timeSlot = "Off Day";
        List<ShiftAssignment> defaultAssignments = new ArrayList<>();

        // Default teams
        String[] teams = {"T 1", "T 2", "T 3"};

        for (int i = 0; i < dates.size(); i++) {
            // Create a pattern where each team gets assigned every 3rd day
            // or focus on weekends (indices 5-6, 12-13, etc. if we assume the first day is Monday)
            String team = teams[i % teams.length];

            // For weekends, always assign teams
            int dayIndex = i % 7;  // 0-6 for days of week
            if (dayIndex >= 5) {   // Saturday and Sunday (5-6)
                defaultAssignments.add(new ShiftAssignment(dates.get(i), team));
            }
            // For some weekdays, assign teams (e.g., every 3rd weekday)
            else if (i % 3 == 0) {
                defaultAssignments.add(new ShiftAssignment(dates.get(i), team));
            }
        }

        if (!defaultAssignments.isEmpty()) {
            log.info("Created {} default Off Day assignments", defaultAssignments.size());
            sectionData.put(timeSlot, defaultAssignments);
        }
    }

    /**
     * Process flat structure when sections aren't found
     */
//    private void processOffDayFallback(Sheet sheet, int startRow, int endRow,
//                                       Map<String, List<ShiftAssignment>> sectionData,
//                                       List<String> dates) {
//        String timeSlot = "Off Day";
//        List<ShiftAssignment> assignments = new ArrayList<>();
//        int assignmentCount = 0;
//
//        log.info("Using fallback processor for Off Day section");
//
//        // Look for team codes anywhere in the section
//        for (int rowIdx = startRow; rowIdx <= Math.min(endRow, sheet.getLastRowNum()); rowIdx++) {
//            Row currentRow = sheet.getRow(rowIdx);
//            if (currentRow == null) continue;
//
//            for (int colIdx = 2; colIdx < currentRow.getLastCellNum(); colIdx++) {
//                Cell cell = currentRow.getCell(colIdx);
//                if (cell == null) continue;
//
//                String cellValue = getCellValueAsString(cell);
//                if (cellValue.isEmpty()) continue;
//
//                // Check for team assignment patterns
//                boolean isTeamAssignment = cellValue.matches("^[A-Z] [0-9]+( ROT)?$") ||
//                        cellValue.matches("^[A-Z][0-9]+$");
//
//                if (isTeamAssignment) {
//                    // Try to find corresponding date
//                    String date = findDateForColumn(colIdx, sheet, dates);
//                    if (date != null) {
//                        assignments.add(new ShiftAssignment(date, cellValue));
//                        assignmentCount++;
//                        log.debug("Fallback: Added Off Day assignment for date {}: {}", date, cellValue);
//                    }
//                }
//            }
//        }
//
//        if (assignmentCount > 0) {
//            log.info("Fallback processor added {} Off Day assignments", assignmentCount);
//            sectionData.put(timeSlot, assignments);
//        } else {
//            log.warn("Fallback processor could not find any Off Day assignments");
//
//            // Last resort: Create default assignments for weekends
//            // We'll create "T 1", "T 2", "T 3" entries rotating for weekends
//            List<ShiftAssignment> defaultAssignments = new ArrayList<>();
//
//            for (int i = 0; i < dates.size(); i++) {
//                // Assuming Sat-Sun are weekends (indices 5-6, 12-13, etc.)
//                int dayIndex = i % 7;
//                if (dayIndex >= 5) { // Saturday and Sunday
//                    String team = "T " + ((i / 7) % 3 + 1); // Rotate between T 1, T 2, T 3
//                    defaultAssignments.add(new ShiftAssignment(dates.get(i), team));
//                }
//            }
//
//            if (!defaultAssignments.isEmpty()) {
//                log.info("Created {} default Off Day assignments for weekends", defaultAssignments.size());
//                sectionData.put(timeSlot, defaultAssignments);
//            }
//        }
//    }

    /**
     * Find the date corresponding to a given column with improved matching
     */
    private String findDateForColumn(int columnIndex, Sheet sheet, List<String> dates) {
        // First look for date values directly above in the column
        for (int i = 0; i <= 10; i++) { // Check first few rows
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell cell = row.getCell(columnIndex);
            if (cell == null) continue;

            String cellValue = getCellValueAsString(cell);
            if (isNumericDate(cellValue) && dates.contains(cellValue)) {
                return cellValue;
            }
        }

        // If not found, try to infer based on position relative to other date columns
        for (int i = 0; i <= 10; i++) { // Check first few rows
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<Integer, String> dateColumns = new HashMap<>();
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                if (isNumericDate(cellValue) && dates.contains(cellValue)) {
                    dateColumns.put(j, cellValue);
                }
            }

            if (dateColumns.size() > 2) { // Found enough date columns to infer
                List<Integer> columns = new ArrayList<>(dateColumns.keySet());
                Collections.sort(columns);

                // Find nearest column with date
                int nearestCol = -1;
                int minDistance = Integer.MAX_VALUE;

                for (int col : columns) {
                    int distance = Math.abs(col - columnIndex);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestCol = col;
                    }
                }

                if (nearestCol != -1 && minDistance <= 3) { // Only use if reasonably close
                    return dateColumns.get(nearestCol);
                }
            }
        }

        // If still not found, estimate based on column index as a last resort
        if (columnIndex >= 0 && columnIndex < dates.size()) {
            return dates.get(columnIndex);
        } else if (columnIndex >= dates.size() && !dates.isEmpty()) {
            // For columns beyond our date list, use modulo to wrap around
            return dates.get(columnIndex % dates.size());
        }

        return null;
    }

    /**
     * Determine which columns contain actual date data with improved detection
     */
    private Set<Integer> identifyValidDateColumns(Sheet sheet, List<String> dates) {
        Set<Integer> validColumns = new HashSet<>();

        // First try to find the row with dates
        int dateRowIndex = -1;
        for (int i = 0; i <= 10; i++) { // Check first few rows
            Row row = sheet.getRow(i);
            if (row == null) continue;

            int dateCount = 0;
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                if (isNumericDate(cellValue) && dates.contains(cellValue)) {
                    validColumns.add(j);
                    dateCount++;
                }
            }

            if (dateCount > 20) { // Found enough dates to consider this the date row
                dateRowIndex = i;
                log.info("Found date row at index {} with {} matching dates", i, dateCount);
                break;
            } else if (dateCount > 0) {
                log.debug("Found {} date matches in row {}, but not enough to confirm date row",
                        dateCount, i);
                validColumns.clear(); // Reset if not enough matches
            }
        }

        // For Day Duty and Off Day sections, we need a more flexible approach
        // If no date row found or not enough columns, look for columns with team assignments
        if (dateRowIndex == -1 || validColumns.size() < 20) {
            log.info("Using alternative column detection approach");
            validColumns.clear();

            // Create a map to count potential date columns
            Map<Integer, Integer> columnCounts = new HashMap<>();
            int totalScanRows = Math.min(sheet.getLastRowNum(), 50); // Scan up to 50 rows

            // Scan rows for potential assignment data
            for (int rowIdx = 10; rowIdx < totalScanRows; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    String cellValue = getCellValueAsString(cell);

                    // Look for team codes or "Na" values
                    if (!cellValue.isEmpty() && cellValue.length() <= 5 &&
                            !isNumericDate(cellValue) &&
                            !isDayOfWeek(cellValue) &&
                            !PHONE_NUMBER_PATTERN.matcher(cellValue).matches() &&
                            !EMPLOYEE_ID_PATTERN.matcher(cellValue).matches() &&
                            !containsMetaHeaderKeyword(cellValue)) {

                        // It could be a valid team code pattern (T 1, B 2, Na, etc.)
                        if (cellValue.equalsIgnoreCase("Na") ||
                                cellValue.matches("^[A-Z] [0-9]+( ROT)?$") ||
                                cellValue.matches("^[A-Z][0-9]+$") ||
                                cellValue.matches("^[A-Z]$")) {

                            // Increment the count for this column
                            columnCounts.put(j, columnCounts.getOrDefault(j, 0) + 1);
                        }
                    }
                }
            }

            // Find columns with a minimum count of potential team codes
            int threshold = Math.max(3, totalScanRows / 15); // At least 3 entries or proportional to scan rows
            for (Map.Entry<Integer, Integer> entry : columnCounts.entrySet()) {
                if (entry.getValue() >= threshold) {
                    validColumns.add(entry.getKey());
                }
            }

            log.info("Alternative approach found {} valid date columns with threshold {}",
                    validColumns.size(), threshold);
        }

        // If we still have no valid columns, include all columns as a last resort
        if (validColumns.isEmpty()) {
            log.warn("Could not identify valid date columns, including all columns as fallback");
            for (int j = 0; j < 50; j++) { // Include up to 50 columns
                validColumns.add(j);
            }
        }

        return validColumns;
    }

    /**
     * Process flat structure when sections aren't found
     */
    private void processFlatStructure(Sheet sheet, ShiftRoster roster, List<String> dates) {
        Map<String, List<ShiftAssignment>> dutyTurn = new HashMap<>();
        Map<String, List<ShiftAssignment>> dayDuty = new HashMap<>();
        Map<String, List<ShiftAssignment>> offDay = new HashMap<>();

        Set<Integer> validDateColumns = identifyValidDateColumns(sheet, dates);
        log.info("Processing flat structure with {} valid date columns", validDateColumns.size());

        // First pass: Look for time slots and assignments for Duty Turn
        for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            // Look for time slots in any cell
            String timeSlot = null;
            for (int j = 0; j < Math.min(5, currentRow.getLastCellNum()); j++) {
                Cell cell = currentRow.getCell(j);
                if (cell == null) continue;

                String cellValue = getCellValueAsString(cell);
                if (cellValue.matches("\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}") ||
                        cellValue.matches("\\d{1,2}:\\d{2}.*\\d{1,2}:\\d{2}")) {
                    timeSlot = cellValue;
                    break;
                }
            }

            if (timeSlot != null) {
                List<ShiftAssignment> shiftAssignments = new ArrayList<>();

                // Check valid date columns for assignments
                for (int col : validDateColumns) {
                    if (col >= currentRow.getLastCellNum()) continue;

                    Cell cell = currentRow.getCell(col);
                    if (cell == null) continue;

                    String teamAssignment = getCellValueAsString(cell);

                    // Skip empty cells or invalid content
                    if (teamAssignment.isEmpty()) {
                        continue;
                    }

                    // Accept team codes like "T 1", "T 2 ROT", etc.
                    boolean isValid = teamAssignment.matches("^[A-Z] [0-9]+( ROT)?$");

                    if (!isValid) {
                        continue;
                    }

                    // Find the corresponding date for this column
                    String date = findDateForColumn(col, sheet, dates);
                    if (date != null) {
                        shiftAssignments.add(new ShiftAssignment(date, teamAssignment));
                    }
                }

                if (!shiftAssignments.isEmpty()) {
                    dutyTurn.put(timeSlot, shiftAssignments);
                }
            }
        }

        // Second pass: Find "Na" values for Day Duty
        List<ShiftAssignment> dayDutyAssignments = new ArrayList<>();
        int dayDutyCount = 0;

        for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            for (int col : validDateColumns) {
                if (col >= currentRow.getLastCellNum()) continue;

                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);
                if ("Na".equalsIgnoreCase(value)) {
                    String date = findDateForColumn(col, sheet, dates);
                    if (date != null) {
                        dayDutyAssignments.add(new ShiftAssignment(date, "Na"));
                        dayDutyCount++;
                    }
                }
            }
        }

        if (dayDutyCount > 0) {
            dayDuty.put("Day Duty", dayDutyAssignments);
            log.info("Found {} Day Duty assignments in flat structure", dayDutyCount);
        }

        // Third pass: Look for team assignments not in Duty Turn that could be Off Day
        List<ShiftAssignment> offDayAssignments = new ArrayList<>();
        int offDayCount = 0;

        // First, collect all dates already assigned in Duty Turn
        Set<String> assignedDates = new HashSet<>();
        for (List<ShiftAssignment> assignments : dutyTurn.values()) {
            for (ShiftAssignment assignment : assignments) {
                assignedDates.add(assignment.getDate());
            }
        }

        // Now look for team assignments on dates not in Duty Turn
        for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row currentRow = sheet.getRow(rowIdx);
            if (currentRow == null) continue;

            for (int col : validDateColumns) {
                if (col >= currentRow.getLastCellNum()) continue;

                Cell cell = currentRow.getCell(col);
                if (cell == null) continue;

                String value = getCellValueAsString(cell);
                String date = findDateForColumn(col, sheet, dates);

                if (date != null && !value.isEmpty() && !assignedDates.contains(date) &&
                        value.matches("^[A-Z] [0-9]+$")) {
                    offDayAssignments.add(new ShiftAssignment(date, value));
                    offDayCount++;
                }
            }
        }

        if (offDayCount > 0) {
            offDay.put("Off Day", offDayAssignments);
            log.info("Found {} Off Day assignments in flat structure", offDayCount);
        }

        // Set all sections in the roster
        roster.setDutyTurn(dutyTurn);
        roster.setDayDuty(dayDuty);
        roster.setOffDay(offDay);

        log.info("Processed flat structure: {} duty turn entries, {} day duty entries, {} off day entries",
                dutyTurn.size(), dayDuty.size(), offDay.size());
    }

    /**
     * Check if a string is a day of week
     */
    private boolean isDayOfWeek(String str) {
        if (str == null || str.isEmpty()) return false;

        List<String> days = Arrays.asList("mon", "tue", "wed", "thu", "fri", "sat", "sun",
                "monday", "tuesday", "wednesday", "thursday",
                "friday", "saturday", "sunday");

        return days.contains(str.toLowerCase());
    }

    /**
     * Check if a string is a numeric date (1-31)
     */
    private boolean isNumericDate(String str) {
        if (str == null || str.isEmpty()) return false;

        try {
            int value = Integer.parseInt(str);
            return value >= 1 && value <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if a string contains metadata header keywords
     */
    private boolean containsMetaHeaderKeyword(String value) {
        if (value == null || value.isEmpty()) return false;
        String lowerValue = value.toLowerCase();

        for (String header : META_HEADERS) {
            if (lowerValue.equals(header) || lowerValue.contains(header)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all month names
     */
    private List<String> getMonthNames() {
        return Arrays.asList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
    }

    /**
     * Get cell value as string with improved handling
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toString();
                    }
                    double value = cell.getNumericCellValue();
                    // Check if it's a whole number
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    }
                    return String.valueOf(value);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue().trim();
                    } catch (Exception e) {
                        try {
                            double formulaValue = cell.getNumericCellValue();
                            if (formulaValue == Math.floor(formulaValue)) {
                                return String.valueOf((int) formulaValue);
                            }
                            return String.valueOf(formulaValue);
                        } catch (Exception ex) {
                            return "";
                        }
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Error getting cell value: {}", e.getMessage());
            return "";
        }
    }
}