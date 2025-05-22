package com.slt.radio.rosterservice.Model.One.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftDto {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private int totalShift;
    private int rotShift;
    private int offDay;
    private int dDuty;
    @Transient
    private String name;
    @Transient
    private String mobileNo;
    @Transient
    private String codeName;

    public int getDDuty() {
        return this.dDuty;
    }

    public void setDDuty(int dDuty) {
        this.dDuty = dDuty;
    }
}