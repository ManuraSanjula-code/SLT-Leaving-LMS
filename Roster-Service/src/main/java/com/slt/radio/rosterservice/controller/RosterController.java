package com.slt.radio.rosterservice.controller;

import com.slt.radio.rosterservice.models.dto.RosterDto;
import com.slt.radio.rosterservice.documents.one.Roster;
import com.slt.radio.rosterservice.documents.one.shift.ShiftRoster;
import com.slt.radio.rosterservice.service.employee.ExcelProcessingService;
import com.slt.radio.rosterservice.service.lms.AttendanceService;
import com.slt.radio.rosterservice.service.RosterServiceE;
import com.slt.radio.rosterservice.service.RosterServiceM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/roster")

public class RosterController {

    @Autowired
    private RosterServiceM rosterServiceM;
    @Autowired
    private RosterServiceE rosterServiceE;
    @Autowired
    private ExcelProcessingService excelProcessingService;
    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/shift-roster/{year}/{month}")
    public ResponseEntity<ShiftRoster> getShiftRosterByDay(@PathVariable int year, @PathVariable String month) {
        return rosterServiceM.getRosterByMonthAndYear(month, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload/employee")
    public ResponseEntity<Roster> uploadEmployee(@RequestParam("file") MultipartFile file) {
        RosterDto processedRoster = excelProcessingService.processExcelFile(file);
        return new ResponseEntity<>(rosterServiceE.createRoster(processedRoster), HttpStatus.CREATED);
    }

    @GetMapping("/{month}/{year}")
    public Roster getMonthlyAttendance(@PathVariable int month, @PathVariable int year) {
        return attendanceService.getMonthlyAttendance(month, year);
    }

    @PostMapping("/upload")
    public ResponseEntity<ShiftRoster> uploadRoster(@RequestParam("file") MultipartFile file) {
        try {
            ShiftRoster processedRoster = rosterServiceM.processRosterFile(file);
            return ResponseEntity.ok(processedRoster);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ShiftRoster>> getAllRosters() {
        return ResponseEntity.ok(rosterServiceM.getAllRosters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftRoster> getRosterById(@PathVariable String id) {
        return rosterServiceM.getRosterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<ShiftRoster> getRosterByMonthAndYear(
            @RequestParam String month,
            @RequestParam int year) {
        return rosterServiceM.getRosterByMonthAndYear(month, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
