package com.slt.radio.rosterservice.service;

import com.slt.radio.rosterservice.model.one.dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.model.one.dto.RosterDto;
import com.slt.radio.rosterservice.model.one.dto.TeamRosterDto;
import com.slt.radio.rosterservice.model.one.employeee.Employee;
import com.slt.radio.rosterservice.model.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.model.one.Roster;
import com.slt.radio.rosterservice.model.one.team.TeamRoster;
import com.slt.radio.rosterservice.repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.repo.EmployeeRepository;
import com.slt.radio.rosterservice.repo.RosterRepository;
import com.slt.radio.rosterservice.service.employee.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RosterServiceE {

    private final RosterRepository rosterRepository;
    private final EmployeeShiftService employeeShiftService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeArchiveRepository employeeArchiveRepository;

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

        Optional<Roster> optional = rosterRepository.findByMonthAndYear(rosterDto.getMonth(), rosterDto.getYear());
        if(optional.isPresent()) return optional.get();

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
                    {
                        log.info("Employee: {}, totalShift: {}, rotShift: {}, offDay: {}, dDuty: {}",
                                emp.getEmployeeId(), emp.getTotalShift(), emp.getRotShift(), emp.getOffDay(), emp.getDDuty());

                        Optional<Employee> employee = employeeRepository.findByEmployeeId(emp.getEmployeeId());

                        if(employee.isPresent()) {
                            log.info("Employee found with employeeId: {}", emp.getEmployeeId());
                            Employee e = employee.get();
                            e.setTeamId(team.getTeamId());
                            employeeRepository.save(e);
                            log.info("Employee saved with teamId: {}", team.getTeamId());
                        } else {
                            log.info("Employee not found with employeeId: {}", emp.getEmployeeId());
                        }
                    }
            );
        });

        return rosterRepository.save(roster);
    }

    // Other methods omitted for brevity
}