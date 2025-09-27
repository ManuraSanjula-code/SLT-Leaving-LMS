package com.slt.radio.rosterservice.model.one;

import com.slt.radio.rosterservice.model.one.team.TeamRoster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rosters")
@CompoundIndex(name = "month_year_idx", def = "{'month': 1, 'year': 1}", unique = true)
public class Roster {
    @Id
    private String id;

    private int month;
    private int year;
    private List<TeamRoster> teams;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}