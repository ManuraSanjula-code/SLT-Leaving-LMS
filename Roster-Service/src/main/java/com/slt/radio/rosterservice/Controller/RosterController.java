package com.slt.radio.rosterservice.Controller;

import com.slt.radio.rosterservice.Model.Dto.RosterDto;
import com.slt.radio.rosterservice.Model.Roster;
import com.slt.radio.rosterservice.Model.Shift.ShiftRoster;
import com.slt.radio.rosterservice.Service.Employee.ExcelProcessingService;
import com.slt.radio.rosterservice.Service.RosterServiceE;
import com.slt.radio.rosterservice.Service.RosterServiceM;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/roster")
@RequiredArgsConstructor
@Slf4j
public class RosterController {

    private final RosterServiceM rosterServiceM;
    private final RosterServiceE rosterServiceE;
    private final ExcelProcessingService excelProcessingService;

    @PostMapping("/upload/employee")
    public ResponseEntity<Roster> uploadEmployee(@RequestParam("file") MultipartFile file) {
        RosterDto processedRoster = excelProcessingService.processExcelFile(file);
        return new ResponseEntity<>(rosterServiceE.createRoster(processedRoster), HttpStatus.CREATED);
    }

    @PostMapping("/upload")
    public ResponseEntity<ShiftRoster> uploadRoster(@RequestParam("file") MultipartFile file) {
        try {
            ShiftRoster processedRoster = rosterServiceM.processRosterFile(file);
            return ResponseEntity.ok(processedRoster);
        } catch (IOException e) {
            log.error("Error processing roster file", e);
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
