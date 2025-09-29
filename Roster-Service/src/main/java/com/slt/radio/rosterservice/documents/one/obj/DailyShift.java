package com.slt.radio.rosterservice.documents.one.obj;

public class DailyShift {
    private int day;
    private String weekday;
    private String shiftCode;

    public DailyShift() {}

    public DailyShift(int day, String weekday, String shiftCode) {
        this.day = day;
        this.weekday = weekday;
        this.shiftCode = shiftCode;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
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

    // Builder pattern
    public static DailyShiftBuilder builder() {
        return new DailyShiftBuilder();
    }

    public static class DailyShiftBuilder {
        private int day;
        private String weekday;
        private String shiftCode;

        public DailyShiftBuilder day(int day) {
            this.day = day;
            return this;
        }

        public DailyShiftBuilder weekday(String weekday) {
            this.weekday = weekday;
            return this;
        }

        public DailyShiftBuilder shiftCode(String shiftCode) {
            this.shiftCode = shiftCode;
            return this;
        }

        public DailyShift build() {
            return new DailyShift(day, weekday, shiftCode);
        }
    }
}