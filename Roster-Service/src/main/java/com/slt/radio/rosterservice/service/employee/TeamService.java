package com.slt.radio.rosterservice.service.employee;


import com.slt.radio.rosterservice.exception.ResourceAlreadyExistsException;
import com.slt.radio.rosterservice.exception.ResourceNotFoundException;
import com.slt.radio.rosterservice.mapper.TeamMapper;
import com.slt.radio.rosterservice.model.one.dto.TeamDto;
import com.slt.radio.rosterservice.model.one.team.Team;
import com.slt.radio.rosterservice.repo.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public List<TeamDto> getAllTeams() {
        return teamMapper.toDtoList(teamRepository.findAll());
    }

    public TeamDto getTeamById(String id) {
        return teamMapper.toDto(findTeamById(id));
    }

    public TeamDto createTeam(TeamDto teamDto) {
        // Check if team with same name already exists
        Optional<Team> existingTeam = teamRepository.findByName(teamDto.getName());
        if (existingTeam.isPresent()) {
            throw new ResourceAlreadyExistsException("Team already exists with name: " + teamDto.getName());
        }

        Team team = teamMapper.toEntity(teamDto);
        team.setActive(true);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto updateTeam(String id, TeamDto teamDto) {
        Team team = findTeamById(id);

        // Check if name is being changed and if it already exists
        if (!team.getName().equals(teamDto.getName())) {
            Optional<Team> existingTeam = teamRepository.findByName(teamDto.getName());
            if (existingTeam.isPresent() && !existingTeam.get().getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Team already exists with name: " + teamDto.getName());
            }
        }

        teamMapper.updateEntityFromDto(teamDto, team);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public void deleteTeam(String id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    public TeamDto deactivateTeam(String id) {
        Team team = findTeamById(id);
        team.setActive(false);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto activateTeam(String id) {
        Team team = findTeamById(id);
        team.setActive(true);
        return teamMapper.toDto(teamRepository.save(team));
    }

    private Team findTeamById(String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }
}

