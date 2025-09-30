package com.slt.radio.rosterservice.document.second;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.DayOfWeek;
import java.util.List;

public class DailyDuty {

    @Field("day_of_week")
    private DayOfWeek dayOfWeek;

    @Field("date")
    private java.time.LocalDate date;

    @Field("time_slots")
    private List<TimeSlot> timeSlots;

    // Constructors
    public DailyDuty() {}

    public DailyDuty(DayOfWeek dayOfWeek, java.time.LocalDate date, List<TimeSlot> timeSlots) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.timeSlots = timeSlots;
    }

    // Getters and Setters
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public java.time.LocalDate getDate() { return date; }
    public void setDate(java.time.LocalDate date) { this.date = date; }

    public List<TimeSlot> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<TimeSlot> timeSlots) { this.timeSlots = timeSlots; }
}