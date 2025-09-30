package com.slt.radio.rosterservice.document.one.teamm;

import com.slt.radio.rosterservice.document.one.obj.EmployeeShift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRoster {
    private String teamId;
    private List<EmployeeShift> employees;
}
