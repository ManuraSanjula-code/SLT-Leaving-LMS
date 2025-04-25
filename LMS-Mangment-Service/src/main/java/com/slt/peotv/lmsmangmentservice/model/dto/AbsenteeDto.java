package com.slt.peotv.lmsmangmentservice.model.dto;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
public class AbsenteeDto {
    private Long id;
    private String publicId;
    private Date date;
    private String employeeID;
    private String userId;
    private Boolean isHalfDay = false;
    private Boolean isFullDay = false;
    private Boolean isSupervisedApproved = false;
    private Boolean isHODApproved = false;
    private Integer audited = 0;
    private Integer isNoPay = 0;
    private Boolean isPending = false;
    private Boolean isAccepted = false;
    private Boolean isLate = false;
    private Boolean isAbsent = false;
    private Boolean isUnSuccessfulAttdate = false;
    private Boolean isLateCover = false;
    private Date happenDate = null; // Explicitly initialized
    private Boolean isArchived = false;
    private String comment = "";
}
