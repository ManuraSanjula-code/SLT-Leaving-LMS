package com.slt.radio.rosterservice.documents.one;

import com.slt.radio.rosterservice.documents.one.team.TeamRoster;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "rosters")
@CompoundIndex(name = "month_year_idx", def = "{'month': 1, 'year': 1}", unique = true)
public class Roster {
    @Id
    private String id;

    private int month;
    private int year;
    private List<TeamRoster> teams;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Roster() {}

    public Roster(String id, int month, int year, List<TeamRoster> teams,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.teams = teams;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<TeamRoster> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamRoster> teams) {
        this.teams = teams;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Roster roster = (Roster) o;
        return month == roster.month &&
                year == roster.year &&
                java.util.Objects.equals(id, roster.id) &&
                java.util.Objects.equals(teams, roster.teams) &&
                java.util.Objects.equals(createdAt, roster.createdAt) &&
                java.util.Objects.equals(updatedAt, roster.updatedAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, month, year, teams, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Roster{" +
                "id='" + id + '\'' +
                ", month=" + month +
                ", year=" + year +
                ", teams=" + teams +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Builder pattern
    public static RosterBuilder builder() {
        return new RosterBuilder();
    }

    public static class RosterBuilder {
        private String id;
        private int month;
        private int year;
        private List<TeamRoster> teams;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public RosterBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RosterBuilder month(int month) {
            this.month = month;
            return this;
        }

        public RosterBuilder year(int year) {
            this.year = year;
            return this;
        }

        public RosterBuilder teams(List<TeamRoster> teams) {
            this.teams = teams;
            return this;
        }

        public RosterBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RosterBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Roster build() {
            return new Roster(id, month, year, teams, createdAt, updatedAt);
        }
    }
}