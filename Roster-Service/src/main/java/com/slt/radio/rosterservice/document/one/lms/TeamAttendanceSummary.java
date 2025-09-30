package com.slt.radio.rosterservice.document.one.lms;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
}
