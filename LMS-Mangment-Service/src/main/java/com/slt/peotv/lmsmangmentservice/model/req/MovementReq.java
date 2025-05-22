package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.model.types.MovementType;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MovementReq {
    private String employeeId;
    private String userId;
    private MovementType movementType;
    private String comment;
    private String destination;
    private String category;
    private Date happenDate;
    private Boolean isAbsent;
    private Boolean isUnSuccessfulAttdate;
    private Boolean isHalfDay;
    private Boolean unAuthorized;
    private Boolean isLate = false;
    private Boolean isLateCover = false;
}
