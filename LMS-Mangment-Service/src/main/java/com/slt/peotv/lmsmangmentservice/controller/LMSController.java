package com.slt.peotv.lmsmangmentservice.controller;

import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.model.req.AttendanceReq;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.utils.service.ApprovalProcessor;
import com.slt.peotv.lmsmangmentservice.utils.service.ExelUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lms")
public class LMSController {

    @Autowired
    public LMS_Service lmsService;

    @Autowired
    private Check_Service checkService;

    @Autowired
    private ExelUtils exelUtils;

    @Autowired
    private ApprovalProcessor threadSafeBulkApprovalService;

    private Date convertStringToDate(String dateString) {
        LocalDate localDate = LocalDate.parse(dateString);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date createDateFromString(String dateString) {
        SimpleDateFormat formatter;

        try {
            // First try with just the date format
            if (dateString.length() <= 10) {
                formatter = new SimpleDateFormat("yyyy-MM-dd");
                return formatter.parse(dateString);
            }
            // Try with the full date-time format
            else {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                return formatter.parse(dateString);
            }
        } catch (ParseException e) {
            // If both attempts fail, try one more lenient approach
            try {
                formatter = new SimpleDateFormat("yyyy-MM-dd");
                return formatter.parse(dateString.substring(0, 10));
            } catch (ParseException ex) {
                System.err.println("Error parsing date: " + ex.getMessage());
                return null;
            }
        }
    }

    @PostMapping("/bulk/approved/movement/{empId}")
    public ResponseEntity<Void> bulkApprovedM(@RequestBody BulkApprovedReq req, @PathVariable String empId) {
        threadSafeBulkApprovalService.allApproved(req, empId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk/approved/leave/{empId}")
    public ResponseEntity<Void> bulkApprovedL(@RequestBody BulkApprovedReq req, @PathVariable String empId) {
        threadSafeBulkApprovalService.allApproved(req, empId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk/reject/movement/{empId}")
    public ResponseEntity<Void> bulkRejectM(@RequestBody BulkApprovedReq req, @PathVariable String empId) {
        checkService.allReject(req, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk/reject/leave/{empId}")
    public ResponseEntity<Void> bulkRejectL(@RequestBody BulkApprovedReq req, @PathVariable String empId) {
        checkService.allReject(req, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/movement/reject/{movementId}/{userId}/{empId}")
    public ResponseEntity<Void> bulkRejectM(@PathVariable String movementId,@PathVariable String userId, @PathVariable String empId) {
        checkService.reject(movementId,userId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave/reject/{leaveId}/{userId}/{empId}")
    public ResponseEntity<Void> bulkRejectL(@PathVariable String leaveId,@PathVariable String userId, @PathVariable String empId) {
        checkService.reject(leaveId,userId, false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{id}/excel/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(10, 99)")
    public ResponseEntity<byte[]> downloadEmployeeExcelReport(@PathVariable String id, @PathVariable String empId) {
        try {
            byte[] excelFile = exelUtils.generateEmployeeExcelReport(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee_report_" + id + ".xlsx\"")
                    .body(excelFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/employee/{id}/excel/date/{actual_date}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(10, 99)")
    public ResponseEntity<byte[]> downloadEmployeeExcelReport(@PathVariable String id, @PathVariable String actual_date, @PathVariable String empId) {
        try {
            byte[] excelFile = exelUtils.generateEmployeeExcelReportByDate(id, createDateFromString(actual_date));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee_report_" + id + ".xlsx\"")
                    .body(excelFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/in-out/{date}/earliest/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Map<String, InOutDTO> getAllInOut(@PathVariable String date, @PathVariable String userId, @PathVariable String empId) {
        return checkService.getEarliestInOut(userId, convertStringToDate(date));
    }

    @GetMapping("/in-out/{date}/{date2}/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public List<InOutDTO> getEarliestInOutBetweenDate(@PathVariable String date, @PathVariable String date2, @PathVariable String userId, @PathVariable String empId) {
        return checkService.getEarliestInOutBetweenDate(userId, convertStringToDate(date), convertStringToDate(date2));
    }

    @GetMapping("/in-out/{date}/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public List<InOutDTO> getEarliestInOutByDate(@PathVariable String date, @PathVariable String userId, @PathVariable String empId) {
        return checkService.getEarliestInOutByDate(userId, convertStringToDate(date));
    }

    @GetMapping("/access-log/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public List<AccessLogRest> getAllAccessLogsToday(@RequestParam String date, @PathVariable String empId) {
        return checkService.getAllAccessLogsToday(date);
    }

    @GetMapping("/access-log")
    public List<AccessLogRest> getAllAccessLogsToday(@RequestParam String date) {
        return checkService.getAllAccessLogsToday(date);
    }

    /// ----------------- ADMIN-----------------------------
    @PostMapping("/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 49)")
    public AttendanceDTO createAttendance(@RequestBody AttendanceReq req, @PathVariable String empId) {
        return lmsService.createAttendance(req);
    }

    @PutMapping("/attendance/{publicId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 49)")
    public AttendanceDTO updateAttendance(@PathVariable String publicId, @RequestBody AttendanceReq req, @PathVariable String empId) {
        System.out.println(publicId);
        return lmsService.updateAttendance(req, publicId);
    }

    @DeleteMapping("/attendance/{publicId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 49)")
    public void deleteAttendance(@PathVariable String publicId, @PathVariable String empId) {
        lmsService.deleteAttendance(publicId);
    }

    @DeleteMapping("/attendance/de/{publicId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 49)")
    public void deleteAttendanceByde(@PathVariable String publicId, @PathVariable String empId) {
        lmsService.deleteAttendanceV1(publicId);
    }

    /// ----------------- ADMIN-----------------------------

    @GetMapping("/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 99)")
    public Page<AttendanceDTO> getAllAttendance(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size, @PathVariable String empId) {
        return lmsService.getAllAttendance(page, size);
    }

    @GetMapping("/un-successful/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AttendanceDTO> getAllAttendanceThatUn(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size, @PathVariable String empId) {
        return lmsService.getAllAttendanceThatUn(page, size);
    }

    @GetMapping("/un-successful/{userid}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AttendanceDTO> getAttendanceThatUnByUserId(@PathVariable String userid, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendanceThatUnByUserId(userid, page, size);
    }

    @GetMapping("/un-authorized/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AttendanceDTO> getAllAttendanceThatUnA(@PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendanceThatUnA(page, size);
    }

    @GetMapping("/un-authorized/{userid}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AttendanceDTO> getAttendanceThatUnAByUserId(@PathVariable String userid, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAttendanceThatUnAByUserId(userid, page, size);
    }

    @GetMapping("/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AttendanceDTO> getAttendanceByUserId(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Boolean isAdmin) {
        return lmsService.getAllAttendanceByUserId(userId, page, size, isAdmin);
    }

    @GetMapping("/leave/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<LeaveDTO> getAllLeaveByUserId(@PathVariable String userId,
                                              @PathVariable String empId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllLeaveByUserByUserId(userId, page, size);
    }

    /// ----------------- ADMIN-----------------------------

    @GetMapping("/leave/admin/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 99)")
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(@PathVariable String userId,
                                                         @PathVariable String empId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllLeaveByUserByUserIdAdmin(userId, page, size);
    }
    /// ----------------- ADMIN-----------------------------

    @GetMapping("/leave/all/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<LeaveDTO> getAllLeave(@PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllLeaves(page, size);
    }

    @DeleteMapping("/leave/{leaveId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public void deleteLeave(@PathVariable String leaveId, @PathVariable String empId) {
        lmsService.deleteLeave(leaveId);
    }

    @GetMapping("/leave-balance/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public UserLeaveDetailsDTO getAllLeaveData(@PathVariable String userId, @PathVariable String empId) {
        return lmsService.getAllLeaveDetails(userId);
    }

    /// ----------------- ADMIN-----------------------------

    @PostMapping("/leave/process/{leaveId}/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(50, 99)")
    public void procesLeave(@PathVariable String leaveId, @PathVariable String userId, @PathVariable String empId) {
        checkService.processLeave(leaveId, userId);
    }
    /// ----------------- ADMIN-----------------------------

    @DeleteMapping("/movement/{movementId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public void deleteMovement(@PathVariable String movementId, @PathVariable String empId) {
        lmsService.deleteMovements(movementId);
    }

    /// ----------------- ADMIN-----------------------------

    @PostMapping("/movement/process/{movementId}/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(50, 99)")
    public void processMovement(@PathVariable String movementId, @PathVariable String userId, @PathVariable String empId) {
        checkService.processMovement(movementId, userId);
    }
    /// ----------------- ADMIN-----------------------------


    @GetMapping("/movement/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<MovementDTO> getAllMovementByUserId(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Boolean isAdmin) {
        return lmsService.getAllMovementByUser(userId, page, size, isAdmin);
    }

    /// ----------------- ADMIN-----------------------------

    @GetMapping("/movement/admin/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 99)")
    public Page<MovementDTO> getAllMovementByAdmin(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,@RequestParam(required = false) Boolean isAdmin) {
        return lmsService.getAllMovementByAdmin(userId, page, size, isAdmin);
    }
    /// ----------------- ADMIN-----------------------------

    @GetMapping("/movement/all/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<MovementDTO> getAllMovement(@PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Boolean isAdmin) {
        return lmsService.getAllMovements(page, size, isAdmin);
    }

    @GetMapping("/no-pay/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<NopayDTO> getAllNopayByUserId(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllNoPayByUser(userId, page, size);
    }

    @GetMapping("/no-pay/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<NopayDTO> getAllNoPays(@PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllNoPays(page, size);
    }

    @GetMapping("/no-pay/user/{userid}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<NopayDTO> getNoPaysByUserId(@PathVariable String userid, @PathVariable String empId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllNoPayByUser(userid, page, size);
    }


    @GetMapping("/in-out/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(10, 199)") /// --- stop
    public Page<InOutDTO> getAllInOutByUserId(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return checkService.getAllInOut(userId, page, size);
    }

    @PostMapping("/management/movement/create/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public ResponseEntity<Void> manageMovement(@PathVariable String empId, @RequestBody MovementReq req, HttpServletRequest request, Authentication authentication) {
        checkService.requestMovement(req, request, authentication);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/management/movement/{movementId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public ResponseEntity<Void> updateMovement(@PathVariable String empId, @RequestBody MovementReq req, @PathVariable String movementId) {
        lmsService.updateMovement(req, movementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/management/leave/create/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public ResponseEntity<Void> manageLeave(@PathVariable String userId,@PathVariable String empId, @RequestBody LeaveReq req, HttpServletRequest request, Authentication authentication) {
        checkService.requestALeave(req, userId, authentication, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/management/leave/{leaveId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public ResponseEntity<Void> updateLeave(@RequestBody LeaveReq req, @PathVariable String leaveId,@PathVariable String empId) {
        lmsService.updateLeave(req, leaveId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/absent/{userId}/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AbsenteeDTO> getAllAbsenteeByUserId(@PathVariable String userId, @PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAbsenteeByUserId(userId, page, size);
    }

    @GetMapping("/absent/all/{empId}")
    @PreAuthorize("@prioritySecurity.hasPriorityInRange(1, 199)")
    public Page<AbsenteeDTO> getAllAbsentee(@PathVariable String empId, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return lmsService.getAllAbsentee(page, size);
    }
}