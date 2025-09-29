package com.slt.radio.rosterservice.documents.one.lms;

public class TeamAttendanceSummary {
    private String teamId;
    private String teamName;
    private String shiftTime;
    private int totalEmployees;
    private int presentEmployees;
    private int lateEmployees;
    private int absentEmployees;
    private int halfDayEmployees;
    private boolean isRotationShift;

    public TeamAttendanceSummary() {}

    public TeamAttendanceSummary(String teamId, String teamName, String shiftTime,
                                 int totalEmployees, int presentEmployees, int lateEmployees,
                                 int absentEmployees, int halfDayEmployees, boolean isRotationShift) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.shiftTime = shiftTime;
        this.totalEmployees = totalEmployees;
        this.presentEmployees = presentEmployees;
        this.lateEmployees = lateEmployees;
        this.absentEmployees = absentEmployees;
        this.halfDayEmployees = halfDayEmployees;
        this.isRotationShift = isRotationShift;
    }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

    public int getPresentEmployees() { return presentEmployees; }
    public void setPresentEmployees(int presentEmployees) { this.presentEmployees = presentEmployees; }

    public int getLateEmployees() { return lateEmployees; }
    public void setLateEmployees(int lateEmployees) { this.lateEmployees = lateEmployees; }

    public int getAbsentEmployees() { return absentEmployees; }
    public void setAbsentEmployees(int absentEmployees) { this.absentEmployees = absentEmployees; }

    public int getHalfDayEmployees() { return halfDayEmployees; }
    public void setHalfDayEmployees(int halfDayEmployees) { this.halfDayEmployees = halfDayEmployees; }

    public boolean isRotationShift() { return isRotationShift; }
    public void setRotationShift(boolean rotationShift) { isRotationShift = rotationShift; }

    @Override
    public String toString() {
        return "TeamAttendanceSummary{" +
                "teamId='" + teamId + '\'' +
                ", teamName='" + teamName + '\'' +
                ", shiftTime='" + shiftTime + '\'' +
                ", totalEmployees=" + totalEmployees +
                ", presentEmployees=" + presentEmployees +
                ", lateEmployees=" + lateEmployees +
                ", absentEmployees=" + absentEmployees +
                ", halfDayEmployees=" + halfDayEmployees +
                ", isRotationShift=" + isRotationShift +
                '}';
    }

    // Builder pattern
    public static TeamAttendanceSummaryBuilder builder() {
        return new TeamAttendanceSummaryBuilder();
    }

    public static class TeamAttendanceSummaryBuilder {
        private String teamId;
        private String teamName;
        private String shiftTime;
        private int totalEmployees;
        private int presentEmployees;
        private int lateEmployees;
        private int absentEmployees;
        private int halfDayEmployees;
        private boolean isRotationShift;

        public TeamAttendanceSummaryBuilder teamId(String teamId) { this.teamId = teamId; return this; }
        public TeamAttendanceSummaryBuilder teamName(String teamName) { this.teamName = teamName; return this; }
        public TeamAttendanceSummaryBuilder shiftTime(String shiftTime) { this.shiftTime = shiftTime; return this; }
        public TeamAttendanceSummaryBuilder totalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; return this; }
        public TeamAttendanceSummaryBuilder presentEmployees(int presentEmployees) { this.presentEmployees = presentEmployees; return this; }
        public TeamAttendanceSummaryBuilder lateEmployees(int lateEmployees) { this.lateEmployees = lateEmployees; return this; }
        public TeamAttendanceSummaryBuilder absentEmployees(int absentEmployees) { this.absentEmployees = absentEmployees; return this; }
        public TeamAttendanceSummaryBuilder halfDayEmployees(int halfDayEmployees) { this.halfDayEmployees = halfDayEmployees; return this; }
        public TeamAttendanceSummaryBuilder isRotationShift(boolean isRotationShift) { this.isRotationShift = isRotationShift; return this; }

        public TeamAttendanceSummary build() {
            return new TeamAttendanceSummary(teamId, teamName, shiftTime, totalEmployees,
                    presentEmployees, lateEmployees, absentEmployees,
                    halfDayEmployees, isRotationShift);
        }
    }
}