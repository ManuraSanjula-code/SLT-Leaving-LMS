package com.slt.radio.rosterservice.Controller.LMS;

import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import com.slt.radio.rosterservice.Model.One.LMS.EmployeeAttendanceDetail;
import com.slt.radio.rosterservice.Model.One.LMS.RosterAttendance;
import com.slt.radio.rosterservice.Model.One.LMS.TeamAttendanceSummary;
import com.slt.radio.rosterservice.Service.LMS.AccessLogSyncService;
import com.slt.radio.rosterservice.Service.LMS.AttendanceService;
import com.slt.radio.rosterservice.Service.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AccessLogSyncService accessLogSyncService;
    private final Service service;

    @DeleteMapping("/{date}/roster")
    public void deleteRoster(@PathVariable("date") String dateStr) {
        service.delete(dateStr, true);
    }

    @DeleteMapping("/{date}/roster-shifts")
    public void deleteRosterShift(@PathVariable("date") String dateStr) {
        service.delete(dateStr, false);
    }

    @PostMapping("/process/{date}")
    public ResponseEntity<RosterAttendance> processAttendanceForDate(
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") String dateStr) {
        RosterAttendance result = attendanceService.processAttendanceForDate(dateStr);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/process/range")
    public ResponseEntity<String> processAttendanceForDateRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        accessLogSyncService.processAttendanceForDateRange(startDate, endDate);
        return ResponseEntity.ok("Processing attendance for date range: " + startDate + " to " + endDate);
    }


    @GetMapping("/summary/team/{date}")
    public ResponseEntity<Map<String, TeamAttendanceSummary>> getRTeamAttendanceSummary(
            @PathVariable("date") String dateStr) {
        Map<String, TeamAttendanceSummary> summary = attendanceService.getRTeamAttendanceSummary(dateStr);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/attendance/{date}")
    public ResponseEntity<List<EmployeeAttendanceDetail>> getRAttendanceSummary(
            @PathVariable("date") String dateStr) {
        List<EmployeeAttendanceDetail> summary = attendanceService.getRAttendanceSummary(dateStr);
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/{date}")
    public ResponseEntity<Page<Attendance>> getAttendanceSummary(
            @PathVariable("date") String dateStr, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Attendance> summary = attendanceService.getAttendanceSummary(dateStr, page, size);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/roster/{date}")
    public ResponseEntity<RosterAttendance> getRoster(
            @PathVariable("date") String dateStr, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(attendanceService.getRoster(dateStr, page, size));
    }


    @GetMapping("/all/roster")
    public Page<RosterAttendance> getMonthlyAttendance(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return attendanceService.getRosterAllAttendance(page, size);
    }


    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getEmployeeAttendance(
            @PathVariable("employeeId") String employeeId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<Attendance> attendances = attendanceService.getEmployeeAttendance(employeeId, startDate, endDate);
        return ResponseEntity.ok(attendances);
    }
}