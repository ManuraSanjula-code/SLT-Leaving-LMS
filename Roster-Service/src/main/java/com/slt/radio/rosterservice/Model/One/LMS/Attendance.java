package com.slt.radio.rosterservice.Model.One.LMS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "attendances")
public class Attendance {
    @Id
    private String id;
    private String publicId;
    private Date date;
    private String employeeID;
    private String teamId;
    private String shiftCode;
    private String shiftTime;
    private String terminalID;
    private Date arrivalDate;
    private String arrivalTime;
    private String leftTime;

    @Builder.Default
    private Boolean isFullDay = false;

    @Builder.Default
    private Boolean isLate = false;

    @Builder.Default
    private Boolean lateCover = false;

    @Builder.Default
    private Boolean isHalfDay = false;

    @Builder.Default
    private Boolean isFullLeave = false;

    @Builder.Default
    private Boolean isShortLeave = false;

    @Builder.Default
    private Boolean isAbsent = false;

    @Builder.Default
    private Boolean isUnSuccessful = false;

    @Builder.Default
    private Boolean isNoPay = false;

    @Builder.Default
    private Boolean issues = false;

    @Builder.Default
    private Boolean isUnAuthorized = false;

    @Builder.Default
    private Boolean resolve = false;

    @Builder.Default
    private Boolean leaveSuccess = false;

    @Builder.Default
    private Boolean leaveReq = false;

    private String issueDescription;
    private Date dueDateForUA;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean noPay = false;

    private String userId;

    @Builder.Default
    private Boolean viaMovement = false;

    @Builder.Default
    private Boolean viaLeave = false;

    @Builder.Default
    private Boolean isOvertimeShift = false; // For ROT shifts

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}