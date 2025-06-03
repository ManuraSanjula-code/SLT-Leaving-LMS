package com.slt.radio.rosterservice.Model.Second;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "duty_rosters")
@CompoundIndex(
        def = "{'isActive': 1}",
        unique = true,
        partialFilter = "{'isActive': true}"
)
public class DutyRoster {

    @Id
    private String id;

    @Field("week_starting_date")
    private LocalDate weekStartingDate;

    @Field("roster_name")
    private String rosterName;

    @Field("daily_duties")
    private List<DailyDuty> dailyDuties;

    @Field("created_date")
    private LocalDate createdDate;

    @Field("updated_date")
    private LocalDate updatedDate;

    private Boolean isActive;

    // Constructors
    public DutyRoster() {}

    public DutyRoster(LocalDate weekStartingDate, String rosterName, List<DailyDuty> dailyDuties, Boolean isActive) {
        this.weekStartingDate = weekStartingDate;
        this.rosterName = rosterName;
        this.dailyDuties = dailyDuties;
        this.createdDate = LocalDate.now();
        this.updatedDate = LocalDate.now();
        this.isActive = isActive;
    }


    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getWeekStartingDate() { return weekStartingDate; }
    public void setWeekStartingDate(LocalDate weekStartingDate) { this.weekStartingDate = weekStartingDate; }

    public String getRosterName() { return rosterName; }
    public void setRosterName(String rosterName) { this.rosterName = rosterName; }

    public List<DailyDuty> getDailyDuties() { return dailyDuties; }
    public void setDailyDuties(List<DailyDuty> dailyDuties) { this.dailyDuties = dailyDuties; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
