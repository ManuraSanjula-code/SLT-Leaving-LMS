package com.slt.radio.rosterservice.Mapper;

import com.slt.radio.rosterservice.Model.Dto.TeamDto;
import com.slt.radio.rosterservice.Model.Teamm.Team;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {
    Team toEntity(TeamDto dto);
    TeamDto toDto(Team entity);
    List<TeamDto> toDtoList(List<Team> entities);
    void updateEntityFromDto(TeamDto dto, @MappingTarget Team entity);
}
