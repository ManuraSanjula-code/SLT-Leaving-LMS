package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.NotBlank;

import java.util.List;

public class TeamRosterDto {
    @NotBlank(message = "Team ID is required")
    private String teamId;

    private List<EmployeeShiftDto> employees;

    public TeamRosterDto() {}

    public TeamRosterDto(String teamId, List<EmployeeShiftDto> employees) {
        this.teamId = teamId;
        this.employees = employees;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public List<EmployeeShiftDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeShiftDto> employees) {
        this.employees = employees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamRosterDto that = (TeamRosterDto) o;
        return java.util.Objects.equals(teamId, that.teamId) &&
                java.util.Objects.equals(employees, that.employees);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(teamId, employees);
    }

    @Override
    public String toString() {
        return "TeamRosterDto{" +
                "teamId='" + teamId + '\'' +
                ", employees=" + employees +
                '}';
    }

    // Builder pattern
    public static TeamRosterDtoBuilder builder() {
        return new TeamRosterDtoBuilder();
    }

    public static class TeamRosterDtoBuilder {
        private String teamId;
        private List<EmployeeShiftDto> employees;

        public TeamRosterDtoBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public TeamRosterDtoBuilder employees(List<EmployeeShiftDto> employees) {
            this.employees = employees;
            return this;
        }

        public TeamRosterDto build() {
            return new TeamRosterDto(teamId, employees);
        }
    }
}