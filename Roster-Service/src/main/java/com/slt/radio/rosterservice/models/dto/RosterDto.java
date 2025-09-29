package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.List;

public class RosterDto {
    private String id;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    private List<TeamRosterDto> teams;

    public RosterDto() {}

    public RosterDto(String id, Integer month, Integer year, List<TeamRosterDto> teams) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.teams = teams;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<TeamRosterDto> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamRosterDto> teams) {
        this.teams = teams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosterDto rosterDto = (RosterDto) o;
        return java.util.Objects.equals(id, rosterDto.id) &&
                java.util.Objects.equals(month, rosterDto.month) &&
                java.util.Objects.equals(year, rosterDto.year) &&
                java.util.Objects.equals(teams, rosterDto.teams);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, month, year, teams);
    }

    @Override
    public String toString() {
        return "RosterDto{" +
                "id='" + id + '\'' +
                ", month=" + month +
                ", year=" + year +
                ", teams=" + teams +
                '}';
    }

    // Builder pattern
    public static RosterDtoBuilder builder() {
        return new RosterDtoBuilder();
    }

    public static class RosterDtoBuilder {
        private String id;
        private Integer month;
        private Integer year;
        private List<TeamRosterDto> teams;

        public RosterDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RosterDtoBuilder month(Integer month) {
            this.month = month;
            return this;
        }

        public RosterDtoBuilder year(Integer year) {
            this.year = year;
            return this;
        }

        public RosterDtoBuilder teams(List<TeamRosterDto> teams) {
            this.teams = teams;
            return this;
        }

        public RosterDto build() {
            return new RosterDto(id, month, year, teams);
        }
    }
}