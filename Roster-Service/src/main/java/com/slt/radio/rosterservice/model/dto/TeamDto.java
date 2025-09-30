package com.slt.radio.rosterservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    private String id;

    @NotBlank(message = "Team name is required")
    private String name;

    private String shortName;
    private boolean active;
}
