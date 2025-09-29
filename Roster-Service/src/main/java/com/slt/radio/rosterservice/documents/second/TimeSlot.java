package com.slt.radio.rosterservice.documents.second;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalTime;
import java.util.List;

public class TimeSlot {

    @Field("start_time")
    private LocalTime startTime;

    @Field("end_time")
    private LocalTime endTime;

    @Field("assigned_employees")
    private List<String> assignedEmployees; // List to handle 1 or 2 employees

    @Field("shift_type")
    private String shiftType; // e.g., "MORNING", "EVENING"

    // Constructors
    public TimeSlot() {}

    public TimeSlot(LocalTime startTime, LocalTime endTime, List<String> assignedEmployees, String shiftType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.assignedEmployees = assignedEmployees;
        this.shiftType = shiftType;
    }

    // Getters and Setters
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public List<String> getAssignedEmployees() { return assignedEmployees; }
    public void setAssignedEmployees(List<String> assignedEmployees) { this.assignedEmployees = assignedEmployees; }

    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }

    // Helper methods
    public boolean hasSingleEmployee() {
        return assignedEmployees != null && assignedEmployees.size() == 1;
    }

    public boolean hasTwoEmployees() {
        return assignedEmployees != null && assignedEmployees.size() == 2;
    }

    public String getPrimaryEmployee() {
        return (assignedEmployees != null && !assignedEmployees.isEmpty()) ? assignedEmployees.get(0) : null;
    }

    public String getSecondaryEmployee() {
        return (assignedEmployees != null && assignedEmployees.size() > 1) ? assignedEmployees.get(1) : null;
    }
}
