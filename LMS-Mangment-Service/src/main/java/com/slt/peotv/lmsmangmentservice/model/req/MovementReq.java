package com.slt.peotv.lmsmangmentservice.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Data
@ToString
public class MovementReq {

    private String publicId;
    private Long id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    private String comment;

    @NotBlank(message = "Destination is required")
    private String destination;

    private String category;

    @NotNull(message = "Happen date is required")
    private Date happenDate;

    @NotNull(message = "Log time is required")
    private Date logTime;

    @NotBlank(message = "In time is required")
    private Time inTime;

    @NotBlank(message = "Out time is required")
    private Time outTime;

    @JsonProperty("inTimeRaw")
    private String inTimeRaw;

    @JsonProperty("outTimeRaw")
    private String outTimeRaw;

    @JsonProperty("happenDateRaw")
    private String happenDateRaw;


    private RequestStatus requestStatus = RequestStatus.DRAFT;

    private Date reqDate;
    private Integer attSync = 0;
    private Long attendanceId;
    private Date createDate;
    private Date updateDate;
    private Boolean isEdited = false;
}