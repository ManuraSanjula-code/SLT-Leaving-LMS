package com.slt.peotv.lmsmangmentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLeaveDetailsDTO {
    private String employeeId;
    private List<LeaveDetailDTO> leaveDetails;
}