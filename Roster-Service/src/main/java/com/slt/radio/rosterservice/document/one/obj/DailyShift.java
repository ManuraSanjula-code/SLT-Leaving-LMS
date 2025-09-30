package com.slt.radio.rosterservice.document.one.obj;

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
