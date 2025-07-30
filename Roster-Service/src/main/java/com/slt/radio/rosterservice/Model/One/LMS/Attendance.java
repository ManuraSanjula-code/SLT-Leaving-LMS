package com.slt.radio.rosterservice.Model.One.LMS;

import java.io.Serializable;
import com.slt.radio.rosterservice.Model.Enum.AttendanceType;
import com.slt.radio.rosterservice.Model.Enum.RosterType;
import com.slt.radio.rosterservice.Model.Enum.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "attendances")
public class Attendance implements Serializable{
    private static final long serialVersionUID = 1328292L;
    
    @Id
    private String id;
    private String publicId;
    private Date date;
    private Date arrivalDate;

    private LocalTime arrivalTime;
    private LocalTime leftTime;

    private String terminalId;
    private String employeeId;

    private String teamId;
    private AttendanceType attendanceType;
    private RosterType rosterType;
    private LeaveStatus leaveStatus;
    @Builder.Default
    private Boolean isLate = false;
    @Builder.Default
    private Boolean isLateCovered = false;
    @Builder.Default
    private Boolean isUnauthorized = false;
    @Builder.Default
    private Boolean isUnSuccessful = false;
    @Builder.Default
    private Boolean isHoliday = false;
    @Builder.Default
    private Boolean isResolved = false;
    @Builder.Default
    private Boolean hasIssues = false;
    @Builder.Default
    private Boolean isManual = false;
    private String issueDescription;
    private Date dueDateForUA;

    private Date etlRunTime = new Date();

    @Builder.Default
    private Date createdDate = new Date();
    @Builder.Default
    private Date updatedDate = new Date();

    @Builder.Default
    private Boolean isActive = true;
    private Boolean viaMovement;
    private Boolean viaLeave;
}