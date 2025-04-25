package com.slt.peotv.lmsmangmentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLeaveDetailsDto {
    private String employeeId;
    private List<LeaveDetailDto> leaveDetails;
}