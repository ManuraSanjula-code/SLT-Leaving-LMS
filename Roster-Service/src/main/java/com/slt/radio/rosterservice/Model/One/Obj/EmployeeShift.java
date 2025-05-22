package com.slt.radio.rosterservice.Model.One.Obj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShift {
    private String employeeId;
    private int totalShift;
    private int rotShift;
    private int offDay;
    private int dDuty;

    public int getDDuty() {
        return this.dDuty;
    }

    public void setDDuty(int dDuty) {
        this.dDuty = dDuty;
    }
}
