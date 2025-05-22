package com.slt.radio.rosterservice.Mapper;

import com.slt.radio.rosterservice.Model.One.Dto.DailyShiftDto;
import com.slt.radio.rosterservice.Model.One.Dto.EmployeeShiftDto;
import com.slt.radio.rosterservice.Model.One.Dto.RosterDto;
import com.slt.radio.rosterservice.Model.One.Dto.TeamRosterDto;
import com.slt.radio.rosterservice.Model.One.Obj.DailyShift;
import com.slt.radio.rosterservice.Model.One.Obj.EmployeeShift;
import com.slt.radio.rosterservice.Model.One.Roster;
import com.slt.radio.rosterservice.Model.One.Teamm.TeamRoster;
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
