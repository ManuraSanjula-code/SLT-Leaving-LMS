package com.slt.radio.rosterservice.service;

import com.slt.radio.rosterservice.document.one.shift.ShiftRoster;
import com.slt.radio.rosterservice.repo.ShiftRosterRepository;
import com.slt.radio.rosterservice.service.roster.ExcelParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RosterServiceM {

    private final ExcelParserService excelParserService;
    private final ShiftRosterRepository rosterRepository;

    public ShiftRoster processRosterFile(MultipartFile file) throws IOException {
        log.info("Processing roster file: {}", file.getOriginalFilename());
        ShiftRoster roster = excelParserService.parseExcelFile(file);

        // Check if a roster for the same month/year already exists
        Optional<ShiftRoster> existingRoster = rosterRepository.findByMonthAndYear(roster.getMonth(), roster.getYear());

        if (existingRoster.isPresent()) {
            // Update existing roster
            ShiftRoster existing = existingRoster.get();
            roster.setId(existing.getId());
            log.info("Updating existing roster for {}, {}", roster.getMonth(), roster.getYear());
        } else {
            log.info("Creating new roster for {}, {}", roster.getMonth(), roster.getYear());
        }

        return rosterRepository.save(roster);
    }

    public List<ShiftRoster> getAllRosters() {
        return rosterRepository.findAll();
    }

    public Optional<ShiftRoster> getRosterById(String id) {
        return rosterRepository.findById(id);
    }

    public Optional<ShiftRoster> getRosterByMonthAndYear(String month, int year) {
        return rosterRepository.findByMonthAndYear(month, year);
    }
}
