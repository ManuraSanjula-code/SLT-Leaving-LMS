package com.slt.radio.rosterservice.Controller.LMS;

import com.slt.radio.rosterservice.Model.Second.DutyRoster;
import com.slt.radio.rosterservice.Model.Second.TimeSlot;
import com.slt.radio.rosterservice.Service.LMS.DutyRosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/duty-roster")
public class DutyRosterController {

    @Autowired
    private DutyRosterService dutyRosterService;

    @PostMapping("/upload")
    public ResponseEntity<DutyRoster> uploadExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("rosterName") String rosterName,
            @RequestParam("weekStartingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartingDate) {

        try {
            DutyRoster roster = dutyRosterService.processExcelFile(file, rosterName, weekStartingDate);
            return ResponseEntity.ok(roster);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/charana-tv/week/{weekStartingDate}")
    public DutyRoster getRosterForWeek(@PathVariable String weekStartingDate ) {
        return dutyRosterService.getTheDuty(weekStartingDate);
    }

    @DeleteMapping("/charana-tv/delete/{day}")
    public ResponseEntity<Void> deleteDuty(@PathVariable String day) {
        dutyRosterService.deleteRosterForWeek(day);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<DutyRoster>> getAllRosters() {
        List<DutyRoster> rosters = dutyRosterService.getAllRosters();
        return ResponseEntity.ok(rosters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DutyRoster> getRosterById(@PathVariable String id) {
        Optional<DutyRoster> roster = dutyRosterService.getRosterById(id);
        return roster.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/week/{weekStartingDate}")
    public ResponseEntity<DutyRoster> getRosterByWeek(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartingDate) {
        Optional<DutyRoster> roster = dutyRosterService.getRosterByWeek(weekStartingDate);
        return roster.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<DutyRoster>> getRostersByEmployee(@PathVariable String employeeId) {
        List<DutyRoster> rosters = dutyRosterService.getRostersByEmployee(employeeId);
        return ResponseEntity.ok(rosters);
    }

    @GetMapping("/employee/{employeeId}/schedule")
    public ResponseEntity<Map<String, List<TimeSlot>>> getEmployeeWeeklySchedule(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        Map<String, List<TimeSlot>> schedule = dutyRosterService.getEmployeeScheduleForWeek(employeeId, weekStart);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<DutyRoster> createRoster(@RequestBody DutyRoster roster) {
        DutyRoster savedRoster = dutyRosterService.saveRoster(roster);
        return ResponseEntity.ok(savedRoster);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DutyRoster> updateRoster(@PathVariable String id, @RequestBody DutyRoster roster) {
        roster.setId(id);
        DutyRoster updatedRoster = dutyRosterService.saveRoster(roster);
        return ResponseEntity.ok(updatedRoster);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoster(@PathVariable String id) {
        dutyRosterService.deleteRoster(id);
        return ResponseEntity.ok().build();
    }
}
