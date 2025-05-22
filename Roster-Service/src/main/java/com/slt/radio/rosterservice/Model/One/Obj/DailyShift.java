package com.slt.radio.rosterservice.Model.One.Obj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyShift {
    private int day;
    private String weekday;
    private String shiftCode;
}
