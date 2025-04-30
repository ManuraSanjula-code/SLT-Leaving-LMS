package com.slt.radio.rosterservice.Service;

import com.slt.radio.rosterservice.Model.*;
import com.slt.radio.rosterservice.Model.Dto.*;
import com.slt.radio.rosterservice.Model.Obj.EmployeeShift;
import com.slt.radio.rosterservice.Model.Teamm.TeamRoster;
import com.slt.radio.rosterservice.Repo.RosterRepository;
import com.slt.radio.rosterservice.Service.Employee.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RosterServiceE {

    private final RosterRepository rosterRepository;
    private final EmployeeShiftService employeeShiftService;

    /**
     * Create a new roster
     * @param rosterDto the roster data
     * @return the created roster
     */
    public Roster createRoster(RosterDto rosterDto) {

        // Log the input DTO
        log.info("Creating roster for month: {}, year: {}, with {} teams",
                rosterDto.getMonth(), rosterDto.getYear(), rosterDto.getTeams().size());

        // Map DTO to entity
        List<TeamRoster> teamRosters = new ArrayList<>();

        for (TeamRosterDto teamRosterDto : rosterDto.getTeams()) {
            List<EmployeeShift> employeeShifts = new ArrayList<>();

            for (EmployeeShiftDto employeeShiftDto : teamRosterDto.getEmployees()) {
                // Use our new service to ensure proper mapping
                EmployeeShift employeeShift = employeeShiftService.mapDtoToEntity(employeeShiftDto);
                employeeShifts.add(employeeShift);
            }

            TeamRoster teamRoster = TeamRoster.builder()
                    .teamId(teamRosterDto.getTeamId())
                    .employees(employeeShifts)
                    .build();

            teamRosters.add(teamRoster);
        }

        Roster roster = Roster.builder()
                .month(rosterDto.getMonth())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .year(rosterDto.getYear())
                .teams(teamRosters)
                .build();

        // Log the entity before saving
        log.info("Saving roster with {} teams", roster.getTeams().size());
        teamRosters.forEach(team -> {
            log.info("Team: {}, with {} employees", team.getTeamId(), team.getEmployees().size());
            team.getEmployees().forEach(emp ->
                    log.info("Employee: {}, totalShift: {}, rotShift: {}, offDay: {}, dDuty: {}",
                            emp.getEmployeeId(), emp.getTotalShift(), emp.getRotShift(), emp.getOffDay(), emp.getDDuty())
            );
        });

        return rosterRepository.save(roster);
    }

    // Other methods omitted for brevity
}