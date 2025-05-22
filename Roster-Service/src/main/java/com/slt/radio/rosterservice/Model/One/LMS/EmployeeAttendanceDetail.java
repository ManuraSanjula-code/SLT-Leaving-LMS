package com.slt.radio.rosterservice.Model.One.LMS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAttendanceDetail {
    private String employeeId;
    private String employeeName;
    private String teamId;
    private String shiftTime;
    private String attendanceStatus; // PRESENT, LATE, ABSENT, HALF_DAY
    private String arrivalTime;
    private String leftTime;
    private Long lateMinutes;
    private boolean isRotationShift;
}

