package com.slt.radio.rosterservice.document.one.shift;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
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
    private List<String> dates; // All dates in the roster
    private Map<String, List<ShiftAssignment>> dutyTurn; // Key is time slot (e.g., "00:00-08:00")
    private Map<String, List<ShiftAssignment>> dayDuty;
    private Map<String, List<ShiftAssignment>> offDay;
}
