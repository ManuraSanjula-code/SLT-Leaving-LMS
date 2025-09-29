package com.slt.radio.rosterservice.documents.one.lms;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "roster_attendances")
@CompoundIndex(name = "month_year_idx", def = "{'month': 1, 'year': 1}", unique = false)
public class RosterAttendance {
    @Id
    private String id;

    private int month;
    private int year;
    private String date;

    private Map<String, TeamAttendanceSummary> teamAttendanceSummary;

    private List<EmployeeAttendanceDetail> employeeAttendanceDetails;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    public RosterAttendance() {}

    public RosterAttendance(String id, int month, int year, String date,
                            Map<String, TeamAttendanceSummary> teamAttendanceSummary,
                            List<EmployeeAttendanceDetail> employeeAttendanceDetails,
                            Date createdAt, Date updatedAt) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.date = date;
        this.teamAttendanceSummary = teamAttendanceSummary;
        this.employeeAttendanceDetails = employeeAttendanceDetails;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Map<String, TeamAttendanceSummary> getTeamAttendanceSummary() { return teamAttendanceSummary; }
    public void setTeamAttendanceSummary(Map<String, TeamAttendanceSummary> teamAttendanceSummary) {
        this.teamAttendanceSummary = teamAttendanceSummary;
    }

    public List<EmployeeAttendanceDetail> getEmployeeAttendanceDetails() { return employeeAttendanceDetails; }
    public void setEmployeeAttendanceDetails(List<EmployeeAttendanceDetail> employeeAttendanceDetails) {
        this.employeeAttendanceDetails = employeeAttendanceDetails;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern
    public static RosterAttendanceBuilder builder() {
        return new RosterAttendanceBuilder();
    }

    public static class RosterAttendanceBuilder {
        private String id;
        private int month;
        private int year;
        private String date;
        private Map<String, TeamAttendanceSummary> teamAttendanceSummary;
        private List<EmployeeAttendanceDetail> employeeAttendanceDetails;
        private Date createdAt;
        private Date updatedAt;

        public RosterAttendanceBuilder id(String id) { this.id = id; return this; }
        public RosterAttendanceBuilder month(int month) { this.month = month; return this; }
        public RosterAttendanceBuilder year(int year) { this.year = year; return this; }
        public RosterAttendanceBuilder date(String date) { this.date = date; return this; }
        public RosterAttendanceBuilder teamAttendanceSummary(Map<String, TeamAttendanceSummary> teamAttendanceSummary) {
            this.teamAttendanceSummary = teamAttendanceSummary; return this;
        }
        public RosterAttendanceBuilder employeeAttendanceDetails(List<EmployeeAttendanceDetail> employeeAttendanceDetails) {
            this.employeeAttendanceDetails = employeeAttendanceDetails; return this;
        }
        public RosterAttendanceBuilder createdAt(Date createdAt) { this.createdAt = createdAt; return this; }
        public RosterAttendanceBuilder updatedAt(Date updatedAt) { this.updatedAt = updatedAt; return this; }

        public RosterAttendance build() {
            return new RosterAttendance(id, month, year, date, teamAttendanceSummary,
                    employeeAttendanceDetails, createdAt, updatedAt);
        }
    }
}