package com.slt.radio.rosterservice.Mapper.Impl;

import com.slt.radio.rosterservice.Mapper.TeamMapper;
import com.slt.radio.rosterservice.Model.One.Dto.TeamDto;
import com.slt.radio.rosterservice.Model.One.Teamm.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public Team toEntity(TeamDto dto) {
        if (dto == null) {
            return null;
        }

        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setShortName(dto.getShortName());
        team.setActive(dto.isActive());

        return team;
    }

    @Override
    public TeamDto toDto(Team entity) {
        if (entity == null) {
            return null;
        }

        TeamDto dto = new TeamDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setShortName(entity.getShortName());
        dto.setActive(entity.isActive());

        return dto;
    }

    @Override
    public List<TeamDto> toDtoList(List<Team> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeamDto dto, Team entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getShortName() != null) {
            entity.setShortName(dto.getShortName());
        }
        entity.setActive(dto.isActive());
    }
}
