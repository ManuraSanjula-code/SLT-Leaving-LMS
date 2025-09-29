package com.slt.radio.rosterservice.service;

import com.slt.radio.rosterservice.models.dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.models.dto.RosterDto;
import com.slt.radio.rosterservice.models.dto.TeamRosterDto;
import com.slt.radio.rosterservice.documents.one.employeee.Employee;
import com.slt.radio.rosterservice.documents.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.documents.one.Roster;
import com.slt.radio.rosterservice.documents.one.team.TeamRoster;
import com.slt.radio.rosterservice.repo.EmployeeArchiveRepository;
import com.slt.radio.rosterservice.repo.EmployeeRepository;
import com.slt.radio.rosterservice.repo.RosterRepository;
import com.slt.radio.rosterservice.service.employee.EmployeeShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RosterServiceE {

    @Autowired
    private RosterRepository rosterRepository;
    @Autowired
    private EmployeeShiftService employeeShiftService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmployeeArchiveRepository employeeArchiveRepository;

    private static final Logger log = LoggerFactory.getLogger(RosterServiceE.class);

    public Roster createRoster(RosterDto rosterDto) {

        log.info("Creating roster for month: {}, year: {}, with {} teams",
                rosterDto.getMonth(), rosterDto.getYear(), rosterDto.getTeams().size());

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

}