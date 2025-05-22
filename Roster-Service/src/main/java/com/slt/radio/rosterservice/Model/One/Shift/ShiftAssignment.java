package com.slt.radio.rosterservice.Model.One.Shift;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftAssignment {
    private String date;
    private String team; // T1, T2, T3, T1 ROT, etc.
}

