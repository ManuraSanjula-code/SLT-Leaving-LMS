package com.slt.radio.rosterservice.mapper.impl;

import com.slt.radio.rosterservice.mapper.RosterMapper;
import com.slt.radio.rosterservice.models.dto.DailyShiftDto;
import com.slt.radio.rosterservice.models.dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.models.dto.RosterDto;
import com.slt.radio.rosterservice.models.dto.TeamRosterDto;
import com.slt.radio.rosterservice.documents.one.obj.DailyShift;
import com.slt.radio.rosterservice.documents.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.documents.one.Roster;
import com.slt.radio.rosterservice.documents.one.team.TeamRoster;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RosterMapperImpl implements RosterMapper {
    @Override
    public Roster toEntity(RosterDto dto) {
        if (dto == null) {
            return null;
        }

        Roster roster = new Roster();
        roster.setId(dto.getId());
        roster.setMonth(dto.getMonth());
        roster.setYear(dto.getYear());

        if (dto.getTeams() != null) {
            List<TeamRoster> teams = dto.getTeams().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            roster.setTeams(teams);
        }

        return roster;
    }

    @Override
    public RosterDto toDto(Roster entity) {
        if (entity == null) {
            return null;
        }

        RosterDto dto = new RosterDto();
        dto.setId(entity.getId());
        dto.setMonth(entity.getMonth());
        dto.setYear(entity.getYear());

        if (entity.getTeams() != null) {
            List<TeamRosterDto> teams = entity.getTeams().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            dto.setTeams(teams);
        }

        return dto;
    }

    @Override
    public TeamRoster toEntity(TeamRosterDto dto) {
        if (dto == null) {
            return null;
        }

        TeamRoster teamRoster = new TeamRoster();
        teamRoster.setTeamId(dto.getTeamId());

        if (dto.getEmployees() != null) {
            List<EmployeeShift> employees = dto.getEmployees().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            teamRoster.setEmployees(employees);
        }

        return teamRoster;
    }

    @Override
    public TeamRosterDto toDto(TeamRoster entity) {
        if (entity == null) {
            return null;
        }

        TeamRosterDto dto = new TeamRosterDto();
        dto.setTeamId(entity.getTeamId());

        if (entity.getEmployees() != null) {
            List<EmployeeShiftDto> employees = entity.getEmployees().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            dto.setEmployees(employees);
        }

        return dto;
    }

    @Override
    public EmployeeShift toEntity(EmployeeShiftDto dto) {
        if (dto == null) {
            return null;
        }

        EmployeeShift employeeShift = new EmployeeShift();
        employeeShift.setEmployeeId(dto.getEmployeeId());
        employeeShift.setTotalShift(dto.getTotalShift());
        employeeShift.setRotShift(dto.getRotShift());
        employeeShift.setOffDay(dto.getOffDay());
        employeeShift.setDDuty(dto.getDDuty());

        return employeeShift;
    }

    @Override
    public EmployeeShiftDto toDto(EmployeeShift entity) {
        if (entity == null) {
            return null;
        }

        EmployeeShiftDto dto = new EmployeeShiftDto();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setTotalShift(entity.getTotalShift());
        dto.setRotShift(entity.getRotShift());
        dto.setOffDay(entity.getOffDay());
        dto.setDDuty(entity.getDDuty());

        return dto;
    }

    @Override
    public DailyShift toEntity(DailyShiftDto dto) {
        if (dto == null) {
            return null;
        }

        DailyShift dailyShift = new DailyShift();
        dailyShift.setDay(dto.getDay());
        dailyShift.setWeekday(dto.getWeekday());
        dailyShift.setShiftCode(dto.getShiftCode());

        return dailyShift;
    }

    @Override
    public DailyShiftDto toDto(DailyShift entity) {
        if (entity == null) {
            return null;
        }

        DailyShiftDto dto = new DailyShiftDto();
        dto.setDay(entity.getDay());
        dto.setWeekday(entity.getWeekday());
        dto.setShiftCode(entity.getShiftCode());

        return dto;
    }

    @Override
    public void updateEntityFromDto(RosterDto dto, Roster entity) {
        if (dto == null) {
            return;
        }

        if (dto.getMonth() != null) {
            entity.setMonth(dto.getMonth());
        }

        if (dto.getYear() != null) {
            entity.setYear(dto.getYear());
        }

        if (dto.getTeams() != null) {
            if (entity.getTeams() == null) {
                entity.setTeams(new ArrayList<>());
            } else {
                entity.getTeams().clear();
            }

            for (TeamRosterDto teamDto : dto.getTeams()) {
                entity.getTeams().add(toEntity(teamDto));
            }
        }
    }
}
