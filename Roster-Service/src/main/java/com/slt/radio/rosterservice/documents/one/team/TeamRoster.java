package com.slt.radio.rosterservice.documents.one.team;

import com.slt.radio.rosterservice.documents.one.obj.EmployeeShift;

import java.util.List;

public class TeamRoster {
    private String teamId;
    private List<EmployeeShift> employees;

    public TeamRoster() {}

    public TeamRoster(String teamId, List<EmployeeShift> employees) {
        this.teamId = teamId;
        this.employees = employees;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public List<EmployeeShift> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeShift> employees) {
        this.employees = employees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamRoster that = (TeamRoster) o;
        return java.util.Objects.equals(teamId, that.teamId) &&
                java.util.Objects.equals(employees, that.employees);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(teamId, employees);
    }

    @Override
    public String toString() {
        return "TeamRoster{" +
                "teamId='" + teamId + '\'' +
                ", employees=" + employees +
                '}';
    }

    // Builder pattern
    public static TeamRosterBuilder builder() {
        return new TeamRosterBuilder();
    }

    public static class TeamRosterBuilder {
        private String teamId;
        private List<EmployeeShift> employees;

        public TeamRosterBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public TeamRosterBuilder employees(List<EmployeeShift> employees) {
            this.employees = employees;
            return this;
        }

        public TeamRoster build() {
            return new TeamRoster(teamId, employees);
        }
    }
}