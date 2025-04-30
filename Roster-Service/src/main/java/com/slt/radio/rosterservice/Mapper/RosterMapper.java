package com.slt.radio.rosterservice.Mapper;

import com.slt.radio.rosterservice.Model.*;
import com.slt.radio.rosterservice.Model.Dto.*;
import com.slt.radio.rosterservice.Model.Obj.DailyShift;
import com.slt.radio.rosterservice.Model.Obj.EmployeeShift;
import com.slt.radio.rosterservice.Model.Teamm.TeamRoster;
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
