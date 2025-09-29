package com.slt.radio.rosterservice.documents.one.shift;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "shift_rosters")
@CompoundIndexes({
        @CompoundIndex(name = "month_year_idx", def = "{'month': 1, 'year': 1}", unique = true)
})
public class ShiftRoster {
    @Id
    private String id;
    private String title;
    private String month;
    private int year;
    private List<String> dates;
    private Map<String, List<ShiftAssignment>> dutyTurn;
    private Map<String, List<ShiftAssignment>> dayDuty;
    private Map<String, List<ShiftAssignment>> offDay;

    public ShiftRoster() {}

    public ShiftRoster(String id, String title, String month, int year, List<String> dates,
                       Map<String, List<ShiftAssignment>> dutyTurn,
                       Map<String, List<ShiftAssignment>> dayDuty,
                       Map<String, List<ShiftAssignment>> offDay) {
        this.id = id;
        this.title = title;
        this.month = month;
        this.year = year;
        this.dates = dates;
        this.dutyTurn = dutyTurn;
        this.dayDuty = dayDuty;
        this.offDay = offDay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public Map<String, List<ShiftAssignment>> getDutyTurn() {
        return dutyTurn;
    }

    public void setDutyTurn(Map<String, List<ShiftAssignment>> dutyTurn) {
        this.dutyTurn = dutyTurn;
    }

    public Map<String, List<ShiftAssignment>> getDayDuty() {
        return dayDuty;
    }

    public void setDayDuty(Map<String, List<ShiftAssignment>> dayDuty) {
        this.dayDuty = dayDuty;
    }

    public Map<String, List<ShiftAssignment>> getOffDay() {
        return offDay;
    }

    public void setOffDay(Map<String, List<ShiftAssignment>> offDay) {
        this.offDay = offDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftRoster that = (ShiftRoster) o;
        return year == that.year &&
                java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(title, that.title) &&
                java.util.Objects.equals(month, that.month) &&
                java.util.Objects.equals(dates, that.dates) &&
                java.util.Objects.equals(dutyTurn, that.dutyTurn) &&
                java.util.Objects.equals(dayDuty, that.dayDuty) &&
                java.util.Objects.equals(offDay, that.offDay);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, title, month, year, dates, dutyTurn, dayDuty, offDay);
    }

    @Override
    public String toString() {
        return "ShiftRoster{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", month='" + month + '\'' +
                ", year=" + year +
                ", dates=" + dates +
                ", dutyTurn=" + dutyTurn +
                ", dayDuty=" + dayDuty +
                ", offDay=" + offDay +
                '}';
    }
}