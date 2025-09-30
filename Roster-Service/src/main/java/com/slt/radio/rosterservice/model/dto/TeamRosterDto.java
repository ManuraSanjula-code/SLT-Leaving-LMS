package com.slt.radio.rosterservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRosterDto {
    @NotBlank(message = "Team ID is required")
    private String teamId;

    private List<EmployeeShiftDto> employees;
}

