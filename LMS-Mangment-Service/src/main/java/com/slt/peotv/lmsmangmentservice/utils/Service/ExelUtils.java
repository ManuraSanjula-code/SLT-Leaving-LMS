package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ExelUtils {
    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private MovementsRepo movementsRepo;

    @Autowired
    private LeaveRepo leaveRepo;

    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;

    @Autowired
    private AbsenteeRepo absenteeRepo;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public byte[] generateEmployeeExcelReport(String id) throws IOException {
        // Find employee
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByPublicId(id));

        if(employeeEntity.isEmpty())
            throw new RuntimeException("Employee not found with ID: " + id);

        EmployeeEntity employee = employeeEntity.get();

        List<AttendanceEntity> attendance = attendanceRepo.findByEmployeeID(employee.getSltId());
        List<LeaveEntity> leaves = leaveRepo.findByEmployeeID(employee.getEmployeeId());
        List<MovementsEntity> movements = movementsRepo.findAllByUserId(employee.getPublicId());
        List<UserLeaveTypeRemainingEntity> remainingLeaves = userLeaveTypeRemainingRepo.findByEmployeeID(employee.getEmployeeId());
        List<AbsenteeEntity> absences = absenteeRepo.findByUserId(employee.getSltId());

        // Create workbook
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create employee info sheet
            createEmployeeInfoSheet(workbook, employee);

            // Create attendance sheet
            createAttendanceSheet(workbook, attendance);

            // Create leaves sheet
            createLeavesSheet(workbook, leaves);

            // Create movements sheet
            createMovementsSheet(workbook, movements);

            // Create remaining leaves sheet
            createRemainingLeavesSheet(workbook, remainingLeaves);

            // Create absences sheet
            createAbsencesSheet(workbook, absences);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateEmployeeExcelReportByDate(String id, Date date) throws IOException {
        // Find employee
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByPublicId(id));

        if(employeeEntity.isEmpty())
            throw new RuntimeException("Employee not found with ID: " + id);

        EmployeeEntity employee = employeeEntity.get();


        List<AttendanceEntity> attendance = attendanceRepo.findByEmployeeIDAndArrivalDateBetween(employee.getSltId(), date, date);

        List<LeaveEntity> leaves = leaveRepo.findByEmployeeIDAndSubmitDateBetween(employee.getEmployeeId(), date,date);

        List<MovementsEntity> movements = movementsRepo.findByEmployeeIdAndReqDateBetween(employee.getEmployeeId(), date,date);

        List<UserLeaveTypeRemainingEntity> remainingLeaves = userLeaveTypeRemainingRepo.findByEmployeeID(employee.getEmployeeId());

        List<AbsenteeEntity> absences = absenteeRepo.findByEmployeeID(employee.getSltId());

        // Create workbook
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create employee info sheet
            createEmployeeInfoSheet(workbook, employee);

            // Create attendance sheet
            createAttendanceSheet(workbook, attendance);

            // Create leaves sheet
            createLeavesSheet(workbook, leaves);

            // Create movements sheet
            createMovementsSheet(workbook, movements);

            // Create remaining leaves sheet
            createRemainingLeavesSheet(workbook, remainingLeaves);

            // Create absences sheet
            createAbsencesSheet(workbook, absences);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createEmployeeInfoSheet(Workbook workbook, EmployeeEntity employee) {
        Sheet sheet = workbook.createSheet("Employee Info");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Create headers
        Row headerRow = sheet.createRow(0);
        createHeaderCell(headerRow, 0, "Attribute", headerStyle);
        createHeaderCell(headerRow, 1, "Value", headerStyle);

        // Create employee data rows
        int rowNum = 1;

        // Database ID
        createDataRow(sheet, rowNum++, "Database ID", employee.getId() != null ? employee.getId().toString() : "");

        // Employee ID
        createDataRow(sheet, rowNum++, "Employee ID", employee.getEmployeeId() != null ? employee.getEmployeeId() : "");

        // Public ID
        createDataRow(sheet, rowNum++, "Public ID", employee.getPublicId() != null ? employee.getPublicId() : "");

        // SLT ID
        createDataRow(sheet, rowNum++, "SLT ID", employee.getSltId() != null ? employee.getSltId() : "");

        // First Name
        createDataRow(sheet, rowNum++, "First Name", employee.getFirstName() != null ? employee.getFirstName() : "");

        // Last Name
        createDataRow(sheet, rowNum++, "Last Name", employee.getLastName() != null ? employee.getLastName() : "");

        // Full Name
        createDataRow(sheet, rowNum++, "Full Name",
                (employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                        (employee.getLastName() != null ? employee.getLastName() : ""));

        // Email
        createDataRow(sheet, rowNum++, "Email", employee.getEmail() != null ? employee.getEmail() : "");

        // Join Date
        createDataRow(sheet, rowNum++, "Join Date",
                employee.getJoin_date() != null ? dateFormat.format(employee.getJoin_date()) : "");

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createAttendanceSheet(Workbook workbook, List<AttendanceEntity> attendanceList) {
        Sheet sheet = workbook.createSheet("Attendance");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Add information row
        Row infoRow = sheet.createRow(0);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("Total Attendance Records: " + (attendanceList != null ? attendanceList.size() : 0));

        // Create headers
        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Date", "Employee ID", "Arrival Date", "Arrival Time", "Left Time",
                "Full Day", "Half Day", "Full Leave", "Short Leave", "Late", "Late Cover",
                "Absent", "Unsuccessful", "No Pay", "Issues", "Unauthorized", "Resolved",
                "Leave Success", "Leave Req", "Issue Description", "Due Date for UA",
                "Active", "NoPay", "User ID", "Via Movement", "Via Leave", "Is Manual"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        // Check if list is empty
        if (attendanceList == null || attendanceList.isEmpty()) {
            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("No attendance records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            // Create data rows
            int rowNum = 2;
            for (AttendanceEntity attendance : attendanceList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // ID
                createCell(row, colNum++, attendance.getId() != null ? attendance.getId().toString() : "");

                // Public ID
                createCell(row, colNum++, attendance.getPublicId() != null ? attendance.getPublicId() : "");

                // Date
                createCell(row, colNum++, attendance.getDate() != null ? dateFormat.format(attendance.getDate()) : "");

                // Employee ID
                createCell(row, colNum++, attendance.getEmployeeID() != null ? attendance.getEmployeeID() : "");

                // Arrival Date
                createCell(row, colNum++, attendance.getArrivalDate() != null ? dateFormat.format(attendance.getArrivalDate()) : "");

                // Arrival Time
                createCell(row, colNum++, attendance.getArrivalTime() != null ? attendance.getArrivalTime().toString() : "");

                // Left Time
                createCell(row, colNum++, attendance.getLeftTime() != null ? attendance.getLeftTime().toString() : "");

                // Boolean fields
                createCell(row, colNum++, attendance.getIsFullDay() != null ? (attendance.getIsFullDay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsHalfDay() != null ? (attendance.getIsHalfDay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsFullLeave() != null ? (attendance.getIsFullLeave() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsShortLeave() != null ? (attendance.getIsShortLeave() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsLate() != null ? (attendance.getIsLate() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getLateCover() != null ? (attendance.getLateCover() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsAbsent() != null ? (attendance.getIsAbsent() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsUnSuccessful() != null ? (attendance.getIsUnSuccessful() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsNoPay() != null ? (attendance.getIsNoPay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIssues() != null ? (attendance.getIssues() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsUnAuthorized() != null ? (attendance.getIsUnAuthorized() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getResolve() != null ? (attendance.getResolve() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getLeaveSuccess() != null ? (attendance.getLeaveSuccess() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getLeaveReq() != null ? (attendance.getLeaveReq() ? "Yes" : "No") : "No");

                // Issue Description
                createCell(row, colNum++, attendance.getIssueDescription() != null ? attendance.getIssueDescription() : "");

                // Due Date for UA
                createCell(row, colNum++, attendance.getDueDateForUA() != null ? dateFormat.format(attendance.getDueDateForUA()) : "");

                // More Boolean fields
                createCell(row, colNum++, attendance.getActive() != null ? (attendance.getActive() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getNopay() != null ? (attendance.getNopay() ? "Yes" : "No") : "No");

                // User ID
                createCell(row, colNum++, attendance.getUserId() != null ? attendance.getUserId() : "");

                // More Boolean fields
                createCell(row, colNum++, attendance.getViaMovement() != null ? (attendance.getViaMovement() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getViaLeave() != null ? (attendance.getViaLeave() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsManual() != null ? (attendance.getIsManual() ? "Yes" : "No") : "No");
            }
        }

        // Apply auto-sizing to all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createLeavesSheet(Workbook workbook, List<LeaveEntity> leavesList) {
        Sheet sheet = workbook.createSheet("Leaves");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Add information row
        Row infoRow = sheet.createRow(0);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("Total Leave Records: " + (leavesList != null ? leavesList.size() : 0));

        // Create headers
        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Employee ID", "Submit Date", "From Date", "To Date",
                "Leave Type", "Is No Pay", "Num of Days", "Description", "Is Half Day",
                "Is Full Day", "Unsuccessful", "Is Unauthorized", "Is Late", "Is Late Cover",
                "Is Short Leave", "Is Pending", "Is Accepted", "Is Absent", "Not Used",
                "Is Canceled", "Is Manual Request", "Happen Date", "User ID"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        // Check if list is empty
        if (leavesList == null || leavesList.isEmpty()) {
            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("No leave records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            // Create data rows
            int rowNum = 2;
            for (LeaveEntity leave : leavesList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // ID
                createCell(row, colNum++, leave.getId() != null ? leave.getId().toString() : "");

                // Public ID
                createCell(row, colNum++, leave.getPublicId() != null ? leave.getPublicId() : "");

                // Employee ID
                createCell(row, colNum++, leave.getEmployeeID() != null ? leave.getEmployeeID() : "");

                // Submit Date
                createCell(row, colNum++, leave.getSubmitDate() != null ? dateFormat.format(leave.getSubmitDate()) : "");

                // From Date
                createCell(row, colNum++, leave.getFromDate() != null ? dateFormat.format(leave.getFromDate()) : "");

                // To Date
                createCell(row, colNum++, leave.getToDate() != null ? dateFormat.format(leave.getToDate()) : "");

                // Leave Type
                createCell(row, colNum++, leave.getLeaveType() != null ? leave.getLeaveType().getName() : "");

                // Is No Pay
                createCell(row, colNum++, leave.getIsNoPay() != null ? leave.getIsNoPay().toString() : "0");

                // Num of Days
                createCell(row, colNum++, leave.getNumOfDays() != null ? leave.getNumOfDays().toString() : "");

                // Description
                createCell(row, colNum++, leave.getDescription() != null ? leave.getDescription() : "");

                // Boolean fields
                createCell(row, colNum++, leave.getIsHalfDay() != null ? (leave.getIsHalfDay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsFullDay() != null ? (leave.getIsFullDay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getUnSuccessful() != null ? (leave.getUnSuccessful() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsUnauthorized() != null ? (leave.getIsUnauthorized() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsLate() != null ? (leave.getIsLate() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsLateCover() != null ? (leave.getIsLateCover() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsShort_Leave() != null ? (leave.getIsShort_Leave() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsPending() != null ? (leave.getIsPending() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsAccepted() != null ? (leave.getIsAccepted() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsAbsent() != null ? (leave.getIsAbsent() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getNotUsed() != null ? (leave.getNotUsed() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsCanceled() != null ? (leave.getIsCanceled() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsManualRequest() != null ? (leave.getIsManualRequest() ? "Yes" : "No") : "No");

                // Happen Date
                createCell(row, colNum++, leave.getHappenDate() != null ? dateFormat.format(leave.getHappenDate()) : "");

                // User ID
                createCell(row, colNum++, leave.getUserId() != null ? leave.getUserId() : "");
            }
        }

        // Apply auto-sizing to all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMovementsSheet(Workbook workbook, List<MovementsEntity> movementsList) {
        Sheet sheet = workbook.createSheet("Movements");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Add information row
        Row infoRow = sheet.createRow(0);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("Total Movement Records: " + (movementsList != null ? movementsList.size() : 0));

        // Create headers
        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "In Time", "Out Time", "Comment", "Log Time", "Category",
                "Destination", "Employee ID", "User ID", "Request Date", "Movement Type",
                "ATT Sync", "Happen Date", "Is Pending", "Is Accepted", "Is Absent",
                "Is Unsuccessful", "Unauthorized", "Resolve", "Is Half Day", "Is Late", "Is Late Cover"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        // Check if list is empty
        if (movementsList == null || movementsList.isEmpty()) {
            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("No movement records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            // Create data rows
            int rowNum = 2;
            for (MovementsEntity movement : movementsList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // ID
                createCell(row, colNum++, movement.getId() != null ? movement.getId().toString() : "");

                // Public ID
                createCell(row, colNum++, movement.getPublicId() != null ? movement.getPublicId() : "");

                // In Time
                createCell(row, colNum++, movement.getInTime() != null ? movement.getInTime() : "");

                // Out Time
                createCell(row, colNum++, movement.getOutTime() != null ? movement.getOutTime() : "");

                // Comment
                createCell(row, colNum++, movement.getComment() != null ? movement.getComment() : "");

                // Log Time
                createCell(row, colNum++, movement.getLogTime() != null ? dateTimeFormat.format(movement.getLogTime()) : "");

                // Category
                createCell(row, colNum++, movement.getCategory() != null ? movement.getCategory() : "");

                // Destination
                createCell(row, colNum++, movement.getDestination() != null ? movement.getDestination() : "");

                // Employee ID
                createCell(row, colNum++, movement.getEmployeeId() != null ? movement.getEmployeeId() : "");

                // User ID
                createCell(row, colNum++, movement.getUserId() != null ? movement.getUserId() : "");

                // Request Date
                createCell(row, colNum++, movement.getReqDate() != null ? dateTimeFormat.format(movement.getReqDate()) : "");

                // Movement Type
                createCell(row, colNum++, movement.getMovementType() != null ? movement.getMovementType().toString() : "");

                // ATT Sync
                createCell(row, colNum++, movement.getAttSync() != null ? movement.getAttSync().toString() : "0");

                // Happen Date
                createCell(row, colNum++, movement.getHappenDate() != null ? dateFormat.format(movement.getHappenDate()) : "");

                // Boolean fields
                createCell(row, colNum++, movement.getIsPending() != null ? (movement.getIsPending() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsAccepted() != null ? (movement.getIsAccepted() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsAbsent() != null ? (movement.getIsAbsent() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsUnSuccessfulAttdate() != null ? (movement.getIsUnSuccessfulAttdate() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getUnAuthorized() != null ? (movement.getUnAuthorized() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getResolve() != null ? (movement.getResolve() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsHalfDay() != null ? (movement.getIsHalfDay() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsLate() != null ? (movement.getIsLate() ? "Yes" : "No") : "No");
                createCell(row, colNum++, movement.getIsLateCover() != null ? (movement.getIsLateCover() ? "Yes" : "No") : "No");
            }
        }

        // Apply auto-sizing to all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createRemainingLeavesSheet(Workbook workbook, List<UserLeaveTypeRemainingEntity> remainingLeavesList) {
        Sheet sheet = workbook.createSheet("Remaining Leaves");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Add information row
        Row infoRow = sheet.createRow(0);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("Total Remaining Leave Records: " + (remainingLeavesList != null ? remainingLeavesList.size() : 0));

        // Create headers
        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Employee ID", "Leave Type", "Remaining Leaves"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        // Check if list is empty
        if (remainingLeavesList == null || remainingLeavesList.isEmpty()) {
            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("No remaining leave records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            // Create data rows
            int rowNum = 2;
            for (UserLeaveTypeRemainingEntity remainingLeave : remainingLeavesList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // ID
                createCell(row, colNum++, remainingLeave.getId() != null ? remainingLeave.getId().toString() : "");

                // Public ID
                createCell(row, colNum++, remainingLeave.getPublicId() != null ? remainingLeave.getPublicId() : "");

                // Employee ID
                createCell(row, colNum++, remainingLeave.getEmployeeID() != null ? remainingLeave.getEmployeeID() : "");

                // Leave Type
                createCell(row, colNum++, remainingLeave.getLeaveType() != null ? remainingLeave.getLeaveType().getName() : "");

                // Remaining Leaves
                createCell(row, colNum++, remainingLeave.getRemainingLeaves() != null ? remainingLeave.getRemainingLeaves().toString() : "0");
            }
        }

        // Apply auto-sizing to all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAbsencesSheet(Workbook workbook, List<AbsenteeEntity> absencesList) {
        Sheet sheet = workbook.createSheet("Absences");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Add information row
        Row infoRow = sheet.createRow(0);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("Total Absence Records: " + (absencesList != null ? absencesList.size() : 0));

        // Create headers
        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Date", "Employee ID", "User ID", "Audited", "Is No Pay"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        // Check if list is empty
        if (absencesList == null || absencesList.isEmpty()) {
            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("No absence records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            // Create data rows
            int rowNum = 2;
            for (AbsenteeEntity absence : absencesList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // ID
                createCell(row, colNum++, absence.getId() != null ? absence.getId().toString() : "");

                // Public ID
                createCell(row, colNum++, absence.getPublicId() != null ? absence.getPublicId() : "");

                // Date
                createCell(row, colNum++, absence.getDate() != null ? dateFormat.format(absence.getDate()) : "");

                // Employee ID
                createCell(row, colNum++, absence.getEmployeeID() != null ? absence.getEmployeeID() : "");

                // User ID
                createCell(row, colNum++, absence.getUserId() != null ? absence.getUserId() : "");

                // Audited
                createCell(row, colNum++, absence.getAudited() != null ? absence.getAudited().toString() : "0");

                // Is No Pay
                createCell(row, colNum++, absence.getIsNoPay() != null ? absence.getIsNoPay().toString() : "0");
            }
        }

        // Apply auto-sizing to all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDataRow(Sheet sheet, int rowNum, String attribute, String value) {
        Row row = sheet.createRow(rowNum);
        createCell(row, 0, attribute);
        createCell(row, 1, value != null ? value : "");
    }

    private void createCell(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
    }

    public byte[] generateEmployeeExcelReportByDateRange(String id, Date startDate, Date endDate) throws IOException {
        // Implementation similar to generateEmployeeExcelReport but with date filters
        // You can add this method when needed
        return null;
    }
}