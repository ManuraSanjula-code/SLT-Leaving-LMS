package com.slt.radio.rosterservice.Service;

import com.slt.radio.rosterservice.Model.One.Dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.Model.One.Dto.RosterDto;
import com.slt.radio.rosterservice.Model.One.Dto.TeamRosterDto;
import com.slt.radio.rosterservice.Model.One.Employeee.Employee;
import com.slt.radio.rosterservice.Model.One.Obj.EmployeeShift;
import com.slt.radio.rosterservice.Model.One.Roster;
import com.slt.radio.rosterservice.Model.One.Teamm.TeamRoster;
import com.slt.radio.rosterservice.Repo.EmployeeRepository;
import com.slt.radio.rosterservice.Repo.RosterRepository;
import com.slt.radio.rosterservice.Service.Employee.EmployeeShiftService;
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