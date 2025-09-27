package com.slt.radio.rosterservice.mapper;

import com.slt.radio.rosterservice.model.one.dto.DailyShiftDto;
import com.slt.radio.rosterservice.model.one.dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.model.one.dto.RosterDto;
import com.slt.radio.rosterservice.model.one.dto.TeamRosterDto;
import com.slt.radio.rosterservice.model.one.obj.DailyShift;
import com.slt.radio.rosterservice.model.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.model.one.Roster;
import com.slt.radio.rosterservice.model.one.team.TeamRoster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RosterMapper {
    Roster toEntity(RosterDto dto);
    RosterDto toDto(Roster entity);
    TeamRoster toEntity(TeamRosterDto dto);
    TeamRosterDto toDto(TeamRoster entity);
    EmployeeShift toEntity(EmployeeShiftDto dto);
    EmployeeShiftDto toDto(EmployeeShift entity);
    DailyShift toEntity(DailyShiftDto dto);
    DailyShiftDto toDto(DailyShift entity);
    void updateEntityFromDto(RosterDto dto, @MappingTarget Roster entity);
}
