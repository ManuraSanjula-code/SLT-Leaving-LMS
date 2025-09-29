package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.NotBlank;

public class TeamDto {
    private String id;

    @NotBlank(message = "Team name is required")
    private String name;

    private String shortName;
    private boolean active;

    public TeamDto() {}

    public TeamDto(String id, String name, String shortName, boolean active) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDto teamDto = (TeamDto) o;
        return active == teamDto.active &&
                java.util.Objects.equals(id, teamDto.id) &&
                java.util.Objects.equals(name, teamDto.name) &&
                java.util.Objects.equals(shortName, teamDto.shortName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, shortName, active);
    }

    @Override
    public String toString() {
        return "TeamDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", active=" + active +
                '}';
    }

    // Builder pattern
    public static TeamDtoBuilder builder() {
        return new TeamDtoBuilder();
    }

    public static class TeamDtoBuilder {
        private String id;
        private String name;
        private String shortName;
        private boolean active;

        public TeamDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public TeamDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamDtoBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public TeamDtoBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamDto build() {
            return new TeamDto(id, name, shortName, active);
        }
    }
}