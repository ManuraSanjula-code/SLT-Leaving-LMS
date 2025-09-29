package com.slt.radio.rosterservice.documents.one.shift;

public class ShiftAssignment {
    private String date;
    private String team;

    public ShiftAssignment() {}

    public ShiftAssignment(String date, String team) {
        this.date = date;
        this.team = team;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "ShiftAssignment{" +
                "date='" + date + '\'' +
                ", team='" + team + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftAssignment that = (ShiftAssignment) o;
        return java.util.Objects.equals(date, that.date) &&
                java.util.Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, team);
    }
}