package com.slt.radio.rosterservice.models.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class DailyShiftDto {
    @NotNull(message = "Day is required")
    @Min(value = 1, message = "Day must be between 1 and 31")
    @Max(value = 31, message = "Day must be between 1 and 31")
    private Integer day;

    private String weekday;
    private String shiftCode;

    public DailyShiftDto() {}

    public DailyShiftDto(Integer day, String weekday, String shiftCode) {
        this.day = day;
        this.weekday = weekday;
        this.shiftCode = shiftCode;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyShiftDto that = (DailyShiftDto) o;
        return java.util.Objects.equals(day, that.day) &&
                java.util.Objects.equals(weekday, that.weekday) &&
                java.util.Objects.equals(shiftCode, that.shiftCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(day, weekday, shiftCode);
    }

    @Override
    public String toString() {
        return "DailyShiftDto{" +
                "day=" + day +
                ", weekday='" + weekday + '\'' +
                ", shiftCode='" + shiftCode + '\'' +
                '}';
    }

    // Builder pattern
    public static DailyShiftDtoBuilder builder() {
        return new DailyShiftDtoBuilder();
    }

    public static class DailyShiftDtoBuilder {
        private Integer day;
        private String weekday;
        private String shiftCode;

        public DailyShiftDtoBuilder day(Integer day) {
            this.day = day;
            return this;
        }

        public DailyShiftDtoBuilder weekday(String weekday) {
            this.weekday = weekday;
            return this;
        }

        public DailyShiftDtoBuilder shiftCode(String shiftCode) {
            this.shiftCode = shiftCode;
            return this;
        }

        public DailyShiftDto build() {
            return new DailyShiftDto(day, weekday, shiftCode);
        }
    }
}