package com.slt.radio.rosterservice.model.one.shift;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShiftAssignment {
    private String date;
    private String team; // T1, T2, T3, T1 ROT, etc.
}

