package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.*;
import com.slt.peotv.lmsmangmentservice.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExelUtils {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    private NoPayRepo noPayRepo;
    @Autowired
    private NoPayReasonRepo noPayReasonRepo;

    public byte[] generateEmployeeExcelReport(String id) throws IOException {
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByPublicId(id));

        if (employeeEntity.isEmpty())
            throw new RuntimeException("Employee not found with ID: " + id);

        EmployeeEntity employee = employeeEntity.get();
        List<AttendanceEntity> attendance = attendanceRepo.findByEmployee(employee);
        List<LeaveEntity> leaves = leaveRepo.findByEmployee(employee);
        List<MovementsEntity> movements = movementsRepo.findAllByEmployee(employee);
        List<UserLeaveTypeRemainingEntity> remainingLeaves = userLeaveTypeRemainingRepo.findByEmployee(employee);

        Page<NoPayEntity> noPayPage = noPayRepo.findByEmployee(employee, PageRequest.of(0, Integer.MAX_VALUE));
        List<NoPayEntity> noPayList = noPayPage.getContent();

        try (Workbook workbook = new XSSFWorkbook()) {
            createEmployeeInfoSheet(workbook, employee);
            createAttendanceSheet(workbook, attendance);
            createLeavesSheet(workbook, leaves);
            createMovementsSheet(workbook, movements);
            createRemainingLeavesSheet(workbook, remainingLeaves);
            createNoPaySheet(workbook, noPayList);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateEmployeeExcelReportByDate(String id, Date date) throws IOException {
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByPublicId(id));

        if (employeeEntity.isEmpty())
            throw new RuntimeException("Employee not found with ID: " + id);

        EmployeeEntity employee = employeeEntity.get();
        List<AttendanceEntity> attendance = attendanceRepo.findByEmployeeAndArrivalDateBetween(employee, date, date);
        List<LeaveEntity> leaves = leaveRepo.findByEmployeeAndSubmitDateBetween(employee, date, date);
        List<MovementsEntity> movements = movementsRepo.findByEmployeeAndReqDateBetween(employee, date, date);
        List<UserLeaveTypeRemainingEntity> remainingLeaves = userLeaveTypeRemainingRepo.findByEmployee(employee);

        Page<NoPayEntity> allNoPayPage = noPayRepo.findByEmployee(employee, PageRequest.of(0, Integer.MAX_VALUE));
        List<NoPayEntity> noPayList = allNoPayPage.getContent().stream()
                .filter(noPay -> {
                    Date noPayDate = noPay.getDate();
                    return noPayDate != null &&
                            dateFormat.format(noPayDate).equals(dateFormat.format(date));
                })
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            createEmployeeInfoSheet(workbook, employee);
            createAttendanceSheet(workbook, attendance);
            createLeavesSheet(workbook, leaves);
            createMovementsSheet(workbook, movements);
            createRemainingLeavesSheet(workbook, remainingLeaves);
            createNoPaySheet(workbook, noPayList);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateEmployeeExcelReportByDateRange(String id, Date startDate, Date endDate) throws IOException {
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findByEmployeeId(id)
                .or(() -> employeeRepo.findBySltId(id))
                .or(() -> employeeRepo.findByPublicId(id));

        if (employeeEntity.isEmpty())
            throw new RuntimeException("Employee not found with ID: " + id);

        EmployeeEntity employee = employeeEntity.get();
        List<AttendanceEntity> attendance = attendanceRepo.findByEmployeeAndArrivalDateBetween(employee, startDate, endDate);
        List<LeaveEntity> leaves = leaveRepo.findByEmployeeAndSubmitDateBetween(employee, startDate, endDate);
        List<MovementsEntity> movements = movementsRepo.findByEmployeeAndReqDateBetween(employee, startDate, endDate);
        List<UserLeaveTypeRemainingEntity> remainingLeaves = userLeaveTypeRemainingRepo.findByEmployee(employee);

        Page<NoPayEntity> allNoPayPage = noPayRepo.findByEmployee(employee, PageRequest.of(0, Integer.MAX_VALUE));
        List<NoPayEntity> noPayList = allNoPayPage.getContent().stream()
                .filter(noPay -> {
                    Date noPayDate = noPay.getDate();
                    return noPayDate != null &&
                            !noPayDate.before(startDate) &&
                            !noPayDate.after(endDate);
                })
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            createEmployeeInfoSheet(workbook, employee);
            createAttendanceSheet(workbook, attendance);
            createLeavesSheet(workbook, leaves);
            createMovementsSheet(workbook, movements);
            createRemainingLeavesSheet(workbook, remainingLeaves);
            createNoPaySheet(workbook, noPayList);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createNoPaySheet(Workbook workbook, List<NoPayEntity> noPayList) {
        Sheet sheet = workbook.createSheet("No Pay");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("Total No Pay Records: " + (noPayList != null ? noPayList.size() : 0));

        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Employee ID", "Attendance ID", "Submission Date",
                "Actual Date", "Comment", "Reasons", "Created Date", "Updated Date",
                "Is Active"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        if (noPayList == null || noPayList.isEmpty()) {
            Row row = sheet.createRow(2);
            row.createCell(0).setCellValue("No pay records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            int rowNum = 2;
            for (NoPayEntity noPay : noPayList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                createCell(row, colNum++, noPay.getId() != null ? noPay.getId().toString() : "");
                createCell(row, colNum++, noPay.getPublicId() != null ? noPay.getPublicId() : "");
                createCell(row, colNum++, noPay.getEmployee() != null ? noPay.getEmployee().getEmployeeId() : "");
                createCell(row, colNum++, noPay.getAttendance() != null ? noPay.getAttendance().getId().toString() : "");
                createCell(row, colNum++, noPay.getSubmissionDate() != null ? dateFormat.format(noPay.getSubmissionDate()) : "");
                createCell(row, colNum++, noPay.getDate() != null ? dateFormat.format(noPay.getDate()) : "");
                createCell(row, colNum++, noPay.getComment() != null ? noPay.getComment() : "");

                String reasons = getNoPayReasons(noPay);
                createCell(row, colNum++, reasons);

                createCell(row, colNum++, noPay.getCreatedDate() != null ? dateTimeFormat.format(noPay.getCreatedDate()) : "");
                createCell(row, colNum++, noPay.getUpdatedDate() != null ? dateTimeFormat.format(noPay.getUpdatedDate()) : "");
                createCell(row, colNum++, noPay.getIsActive() != null ? (noPay.getIsActive() ? "Yes" : "No") : "No");
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String getNoPayReasons(NoPayEntity noPay) {
        try {
            Optional<NoPayReasonEntity> reasonEntity = noPayReasonRepo.findNoPayReasonEntitiesByNoPay(noPay);
            if (reasonEntity.isPresent()) {
                NoPayReason reason = reasonEntity.get().getReason();
                return reason != null ? reason.name() : "";
            }
            return "";
        } catch (Exception e) {
            return "Error retrieving reason";
        }
    }

    private void createEmployeeInfoSheet(Workbook workbook, EmployeeEntity employee) {
        Sheet sheet = workbook.createSheet("Employee Info");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createHeaderCell(headerRow, 0, "Attribute", headerStyle);
        createHeaderCell(headerRow, 1, "Value", headerStyle);

        int rowNum = 1;
        createDataRow(sheet, rowNum++, "Database ID", employee.getId() != null ? employee.getId().toString() : "");
        createDataRow(sheet, rowNum++, "Employee ID", employee.getEmployeeId() != null ? employee.getEmployeeId() : "");
        createDataRow(sheet, rowNum++, "Public ID", employee.getPublicId() != null ? employee.getPublicId() : "");
        createDataRow(sheet, rowNum++, "SLT ID", employee.getSltId() != null ? employee.getSltId() : "");
        createDataRow(sheet, rowNum++, "First Name", employee.getFirstName() != null ? employee.getFirstName() : "");
        createDataRow(sheet, rowNum++, "Last Name", employee.getLastName() != null ? employee.getLastName() : "");
        createDataRow(sheet, rowNum++, "Full Name",
                (employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                        (employee.getLastName() != null ? employee.getLastName() : ""));
        createDataRow(sheet, rowNum++, "Email", employee.getEmail() != null ? employee.getEmail() : "");
        createDataRow(sheet, rowNum++, "Join Date",
                employee.getJoin_date() != null ? dateFormat.format(employee.getJoin_date()) : "");
        createDataRow(sheet, rowNum++, "Roaster",
                employee.getRoaster() != null ? (employee.getRoaster() ? "Yes" : "No") : "No");
        createDataRow(sheet, rowNum++, "Profile Pic",
                employee.getProfilePic() != null ? employee.getProfilePic() : "");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createAttendanceSheet(Workbook workbook, List<AttendanceEntity> attendanceList) {
        Sheet sheet = workbook.createSheet("Attendance");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("Total Attendance Records: " + (attendanceList != null ? attendanceList.size() : 0));

        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Date", "Employee ID", "Arrival Date", "Arrival Time", "Left Time",
                "Terminal ID", "Attendance Type", "Leave Status", "Pay Status", "Resolve Type",
                "Is Late", "Is Late Covered", "Is Unauthorized", "Is Unsuccessful", "Is Holiday",
                "Is Resolved", "Has Issues", "Is Manual", "Issue Description", "Due Date for UA",
                "ETL Run Time", "Created Date", "Updated Date", "Is Active", "Via Movement", "Via Leave"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        if (attendanceList == null || attendanceList.isEmpty()) {
            Row row = sheet.createRow(2);
            row.createCell(0).setCellValue("No attendance records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            int rowNum = 2;
            for (AttendanceEntity attendance : attendanceList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                createCell(row, colNum++, attendance.getId() != null ? attendance.getId().toString() : "");
                createCell(row, colNum++, attendance.getPublicId() != null ? attendance.getPublicId() : "");
                createCell(row, colNum++, attendance.getDate() != null ? dateFormat.format(attendance.getDate()) : "");
                createCell(row, colNum++, attendance.getEmployee() != null ? attendance.getEmployee().getEmployeeId() : "");
                createCell(row, colNum++, attendance.getArrivalDate() != null ? dateFormat.format(attendance.getArrivalDate()) : "");
                createCell(row, colNum++, attendance.getArrivalTime() != null ? attendance.getArrivalTime().toString() : "");
                createCell(row, colNum++, attendance.getLeftTime() != null ? attendance.getLeftTime().toString() : "");
                createCell(row, colNum++, attendance.getTerminalId() != null ? attendance.getTerminalId() : "");
                createCell(row, colNum++, attendance.getAttendanceType() != null ? attendance.getAttendanceType().getDescription() : "");
                createCell(row, colNum++, attendance.getLeaveStatus() != null ? attendance.getLeaveStatus().getDescription() : "");
                createCell(row, colNum++, attendance.getPayStatus() != null ? attendance.getPayStatus().getDescription() : "");
                createCell(row, colNum++, attendance.getResolve() != null ? attendance.getResolve().getDescription() : "");
                createCell(row, colNum++, attendance.getIsLate() != null ? (attendance.getIsLate() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsLateCovered() != null ? (attendance.getIsLateCovered() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsUnauthorized() != null ? (attendance.getIsUnauthorized() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsUnSuccessful() != null ? (attendance.getIsUnSuccessful() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsHoliday() != null ? (attendance.getIsHoliday() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsResolved() != null ? (attendance.getIsResolved() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getHasIssues() != null ? (attendance.getHasIssues() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIsManual() != null ? (attendance.getIsManual() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getIssueDescription() != null ? attendance.getIssueDescription() : "");
                createCell(row, colNum++, attendance.getDueDateForUA() != null ? dateFormat.format(attendance.getDueDateForUA()) : "");
                createCell(row, colNum++, attendance.getEtlRunTime() != null ? dateTimeFormat.format(attendance.getEtlRunTime()) : "");
                createCell(row, colNum++, attendance.getCreatedDate() != null ? dateTimeFormat.format(attendance.getCreatedDate()) : "");
                createCell(row, colNum++, attendance.getUpdatedDate() != null ? dateTimeFormat.format(attendance.getUpdatedDate()) : "");
                createCell(row, colNum++, attendance.getIsActive() != null ? (attendance.getIsActive() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getViaMovement() != null ? (attendance.getViaMovement() ? "Yes" : "No") : "No");
                createCell(row, colNum++, attendance.getViaLeave() != null ? (attendance.getViaLeave() ? "Yes" : "No") : "No");
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createLeavesSheet(Workbook workbook, List<LeaveEntity> leavesList) {
        Sheet sheet = workbook.createSheet("Leaves");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("Total Leave Records: " + (leavesList != null ? leavesList.size() : 0));

        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "Employee ID", "Submit Date", "From Date", "To Date",
                "Leave Type", "Num of Days", "Description", "Component Behavior",
                "Request Status", "Not Used", "Is Manual Request", "Happen Date",
                "Create Date", "Update Date", "Is Edited", "Attendance ID"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        if (leavesList == null || leavesList.isEmpty()) {
            Row row = sheet.createRow(2);
            row.createCell(0).setCellValue("No leave records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            int rowNum = 2;
            for (LeaveEntity leave : leavesList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                createCell(row, colNum++, leave.getId() != null ? leave.getId().toString() : "");
                createCell(row, colNum++, leave.getPublicId() != null ? leave.getPublicId() : "");
                createCell(row, colNum++, leave.getEmployee() != null ? leave.getEmployee().getEmployeeId() : "");
                createCell(row, colNum++, leave.getSubmitDate() != null ? dateFormat.format(leave.getSubmitDate()) : "");
                createCell(row, colNum++, leave.getFromDate() != null ? dateFormat.format(leave.getFromDate()) : "");
                createCell(row, colNum++, leave.getToDate() != null ? dateFormat.format(leave.getToDate()) : "");
                createCell(row, colNum++, leave.getLeaveType() != null ? leave.getLeaveType().getName() : "");
                createCell(row, colNum++, leave.getNumOfDays() != null ? leave.getNumOfDays().toString() : "");
                createCell(row, colNum++, leave.getDescription() != null ? leave.getDescription() : "");
                createCell(row, colNum++, leave.getComponentBehavior() != null ? leave.getComponentBehavior().name() : "");
                createCell(row, colNum++, leave.getRequestStatus() != null ? leave.getRequestStatus().getDescription() : "");
                createCell(row, colNum++, leave.getNotUsed() != null ? (leave.getNotUsed() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getIsManualRequest() != null ? (leave.getIsManualRequest() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getHappenDate() != null ? dateFormat.format(leave.getHappenDate()) : "");
                createCell(row, colNum++, leave.getCreateDate() != null ? dateTimeFormat.format(leave.getCreateDate()) : "");
                createCell(row, colNum++, leave.getUpdateDate() != null ? dateTimeFormat.format(leave.getUpdateDate()) : "");
                createCell(row, colNum++, leave.getIsEdited() != null ? (leave.getIsEdited() ? "Yes" : "No") : "No");
                createCell(row, colNum++, leave.getAttendance() != null ? leave.getAttendance().getId().toString() : "");
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMovementsSheet(Workbook workbook, List<MovementsEntity> movementsList) {
        Sheet sheet = workbook.createSheet("Movements");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("Total Movement Records: " + (movementsList != null ? movementsList.size() : 0));

        Row headerRow = sheet.createRow(1);
        String[] headers = {
                "ID", "Public ID", "In Time", "Out Time", "Comment", "Log Time", "Category",
                "Destination", "Employee ID", "Request Date", "Movement Type", "ATT Sync",
                "Happen Date", "Request Status", "Attendance ID", "Create Date", "Update Date",
                "Is Edited"
        };

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        if (movementsList == null || movementsList.isEmpty()) {
            Row row = sheet.createRow(2);
            row.createCell(0).setCellValue("No movement records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            int rowNum = 2;
            for (MovementsEntity movement : movementsList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                createCell(row, colNum++, movement.getId() != null ? movement.getId().toString() : "");
                createCell(row, colNum++, movement.getPublicId() != null ? movement.getPublicId() : "");
                createCell(row, colNum++, movement.getInTimeRaw() != null ? movement.getInTimeRaw() : "");
                createCell(row, colNum++, movement.getOutTimeRaw() != null ? movement.getOutTimeRaw() : "");
                createCell(row, colNum++, movement.getComment() != null ? movement.getComment() : "");
                createCell(row, colNum++, movement.getLogTime() != null ? dateTimeFormat.format(movement.getLogTime()) : "");
                createCell(row, colNum++, movement.getCategory() != null ? movement.getCategory() : "");
                createCell(row, colNum++, movement.getDestination() != null ? movement.getDestination() : "");
                createCell(row, colNum++, movement.getEmployee() != null ? movement.getEmployee().getEmployeeId() : "");
                createCell(row, colNum++, movement.getReqDate() != null ? dateTimeFormat.format(movement.getReqDate()) : "");
                createCell(row, colNum++, movement.getMovementType() != null ? movement.getMovementType().name() : "");
                createCell(row, colNum++, movement.getAttSync() != null ? movement.getAttSync().toString() : "0");
                createCell(row, colNum++, movement.getHappenDate() != null ? dateFormat.format(movement.getHappenDate()) : "");
                createCell(row, colNum++, movement.getRequestStatus() != null ? movement.getRequestStatus().getDescription() : "");
                createCell(row, colNum++, movement.getAttendance() != null ? movement.getAttendance().getId().toString() : "");
                createCell(row, colNum++, movement.getCreateDate() != null ? dateTimeFormat.format(movement.getCreateDate()) : "");
                createCell(row, colNum++, movement.getUpdateDate() != null ? dateTimeFormat.format(movement.getUpdateDate()) : "");
                createCell(row, colNum++, movement.getIsEdited() != null ? (movement.getIsEdited() ? "Yes" : "No") : "No");
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createRemainingLeavesSheet(Workbook workbook, List<UserLeaveTypeRemainingEntity> remainingLeavesList) {
        Sheet sheet = workbook.createSheet("Remaining Leaves");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("Total Remaining Leave Records: " + (remainingLeavesList != null ? remainingLeavesList.size() : 0));

        Row headerRow = sheet.createRow(1);
        String[] headers = {"ID", "Public ID", "Employee ID", "Leave Type", "Remaining Leaves"};

        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        if (remainingLeavesList == null || remainingLeavesList.isEmpty()) {
            Row row = sheet.createRow(2);
            row.createCell(0).setCellValue("No remaining leave records found");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.length - 1));
        } else {
            int rowNum = 2;
            for (UserLeaveTypeRemainingEntity remainingLeave : remainingLeavesList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                createCell(row, colNum++, remainingLeave.getId() != null ? remainingLeave.getId().toString() : "");
                createCell(row, colNum++, remainingLeave.getPublicId() != null ? remainingLeave.getPublicId() : "");
                createCell(row, colNum++, remainingLeave.getEmployee() != null ? remainingLeave.getEmployee().getEmployeeId() : "");
                createCell(row, colNum++, remainingLeave.getLeaveType() != null ? remainingLeave.getLeaveType().getName() : "");
                createCell(row, colNum++, remainingLeave.getRemainingLeaves() != null ? remainingLeave.getRemainingLeaves().toString() : "0");
            }
        }

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
}