package com.slt.radio.rosterservice.documents.one.team;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "teams")
public class Team {
    @Id
    private String id;

    private String name;
    private String shortName;
    private boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Team() {}

    public Team(String id, String name, String shortName, boolean active,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return active == team.active &&
                java.util.Objects.equals(id, team.id) &&
                java.util.Objects.equals(name, team.name) &&
                java.util.Objects.equals(shortName, team.shortName) &&
                java.util.Objects.equals(createdAt, team.createdAt) &&
                java.util.Objects.equals(updatedAt, team.updatedAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, shortName, active, createdAt, updatedAt);
    }

    // Builder pattern
    public static TeamBuilder builder() {
        return new TeamBuilder();
    }

    public static class TeamBuilder {
        private String id;
        private String name;
        private String shortName;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TeamBuilder id(String id) {
            this.id = id;
            return this;
        }

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public TeamBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TeamBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TeamBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Team build() {
            return new Team(id, name, shortName, active, createdAt, updatedAt);
        }
    }
}