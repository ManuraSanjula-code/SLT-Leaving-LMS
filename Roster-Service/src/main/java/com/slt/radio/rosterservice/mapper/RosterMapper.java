package com.slt.radio.rosterservice.mapper;

import com.slt.radio.rosterservice.model.dto.DailyShiftDto;
import com.slt.radio.rosterservice.model.dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.model.dto.RosterDto;
import com.slt.radio.rosterservice.model.dto.TeamRosterDto;
import com.slt.radio.rosterservice.document.one.obj.DailyShift;
import com.slt.radio.rosterservice.document.one.obj.EmployeeShift;
import com.slt.radio.rosterservice.document.one.Roster;
import com.slt.radio.rosterservice.document.one.teamm.TeamRoster;
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
