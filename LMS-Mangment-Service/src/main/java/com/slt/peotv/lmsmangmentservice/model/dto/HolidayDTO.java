package com.slt.peotv.lmsmangmentservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HolidayDTO {
    private Long id;
    private LocalDate holidayDate;
    private String description;
    private boolean isRecurring = false;
    private LocalDateTime createdAt;
    public HolidayDTO() {
    }

    public HolidayDTO(LocalDate holidayDate, String description, boolean isRecurring) {
        this.holidayDate = holidayDate;
        this.description = description;
        this.isRecurring = isRecurring;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // toString() method
    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", holidayDate=" + holidayDate +
                ", description='" + description + '\'' +
                ", isRecurring=" + isRecurring +
                ", createdAt=" + createdAt +
                '}';
    }
}