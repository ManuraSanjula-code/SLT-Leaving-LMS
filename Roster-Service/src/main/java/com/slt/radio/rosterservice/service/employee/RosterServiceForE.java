package com.slt.radio.rosterservice.service.employee;

import com.slt.radio.rosterservice.document.one.Roster;
import com.slt.radio.rosterservice.exception.ResourceAlreadyExistsException;
import com.slt.radio.rosterservice.exception.ResourceNotFoundException;
import com.slt.radio.rosterservice.mapper.RosterMapper;
import com.slt.radio.rosterservice.model.dto.RosterDto;
import com.slt.radio.rosterservice.repo.RosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RosterServiceForE {
    private final RosterRepository rosterRepository;
    private final RosterMapper rosterMapper;

    public List<Roster> getAllRosters() {
        return rosterRepository.findAll();
    }

    public RosterDto getRosterById(String id) {
        return rosterMapper.toDto(findRosterById(id));
    }

    public RosterDto getRosterByMonthAndYear(int month, int year) {
        return rosterRepository.findByMonthAndYear(month, year)
                .map(rosterMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Roster not found for month: " + month + " and year: " + year));
    }

    public RosterDto createRoster(RosterDto rosterDto) {
        // Check if roster already exists for the month and year
        Optional<Roster> existingRoster = rosterRepository.findByMonthAndYear(rosterDto.getMonth(), rosterDto.getYear());
        if (existingRoster.isPresent()) {
            throw new ResourceAlreadyExistsException("Roster already exists for month: " + rosterDto.getMonth() + " and year: " + rosterDto.getYear());
        }

        // Validate month and year
        validateMonthAndYear(rosterDto.getMonth(), rosterDto.getYear());

        Roster roster = rosterMapper.toEntity(rosterDto);
        return rosterMapper.toDto(rosterRepository.save(roster));
    }

    public RosterDto updateRoster(String id, RosterDto rosterDto) {
        Roster roster = findRosterById(id);

        // If month or year is being changed, check if a roster already exists for the new month and year
        if (roster.getMonth() != rosterDto.getMonth() || roster.getYear() != rosterDto.getYear()) {
            Optional<Roster> existingRoster = rosterRepository.findByMonthAndYear(rosterDto.getMonth(), rosterDto.getYear());
            if (existingRoster.isPresent() && !existingRoster.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Roster already exists for month: " + rosterDto.getMonth() + " and year: " + rosterDto.getYear());
            }

            // Validate month and year
            validateMonthAndYear(rosterDto.getMonth(), rosterDto.getYear());
        }

        rosterMapper.updateEntityFromDto(rosterDto, roster);
        return rosterMapper.toDto(rosterRepository.save(roster));
    }

    public void deleteRoster(String id) {
        if (!rosterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Roster not found with id: " + id);
        }
        rosterRepository.deleteById(id);
    }

    private Roster findRosterById(String id) {
        return rosterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roster not found with id: " + id));
    }

    private void validateMonthAndYear(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        if (year < 2000) {
            throw new IllegalArgumentException("Year must be 2000 or later");
        }

        // Ensure the month and year combination is valid (not in the future)
        YearMonth current = YearMonth.now();
        YearMonth requested = YearMonth.of(year, month);

        if (requested.isAfter(current.plusMonths(1))) {
            throw new IllegalArgumentException("Cannot create roster more than one month in the future");
        }
    }
}
