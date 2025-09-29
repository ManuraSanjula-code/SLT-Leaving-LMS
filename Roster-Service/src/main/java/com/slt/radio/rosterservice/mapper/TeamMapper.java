package com.slt.radio.rosterservice.mapper;

import com.slt.radio.rosterservice.models.dto.TeamDto;
import com.slt.radio.rosterservice.documents.one.team.Team;
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
