package com.slt.radio.rosterservice.Controller;
;
import com.slt.radio.rosterservice.Model.Dto.TeamDto;
import com.slt.radio.rosterservice.Service.Employee.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeamById(@PathVariable String id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody TeamDto teamDto) {
        return new ResponseEntity<>(teamService.createTeam(teamDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable String id, @Valid @RequestBody TeamDto teamDto) {
        return ResponseEntity.ok(teamService.updateTeam(id, teamDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<TeamDto> deactivateTeam(@PathVariable String id) {
        return ResponseEntity.ok(teamService.deactivateTeam(id));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<TeamDto> activateTeam(@PathVariable String id) {
        return ResponseEntity.ok(teamService.activateTeam(id));
    }
}
