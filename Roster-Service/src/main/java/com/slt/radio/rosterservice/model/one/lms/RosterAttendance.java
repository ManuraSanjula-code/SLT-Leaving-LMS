package com.slt.radio.rosterservice.model.one.lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roster_attendances")
@CompoundIndex(name = "month_year_idx", def = "{'month': 1, 'year': 1}", unique = false)
public class RosterAttendance {
    @Id
    private String id;

    private int month;
    private int year;
    private String date; // Format: YYYY-MM-DD

    // Team-based attendance summary
    private Map<String, TeamAttendanceSummary> teamAttendanceSummary;

    // Employee-based attendance details
    private List<EmployeeAttendanceDetail> employeeAttendanceDetails;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}
