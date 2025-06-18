package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private String inTime = "00:00";

    @NotBlank(message = "Out time is required")
    private String outTime = "00:00";

    private RequestStatus requestStatus = RequestStatus.DRAFT;

    private Date reqDate;
    private Integer attSync = 0;
    private Long attendanceId;
    private Date createDate;
    private Date updateDate;
    private Boolean isEdited = false;



    public boolean validateMovementReq() {
        // Basic null and empty checks
        if (Objects.isNull(this.employeeId) || this.employeeId.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.userId) || this.userId.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.movementType)) {
            return false;
        }

        if (Objects.isNull(this.destination) || this.destination.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.happenDate)) {
            return false;
        }

        if (Objects.isNull(this.logTime)) {
            return false;
        }

        if (Objects.isNull(this.inTime) || this.inTime.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.outTime) || this.outTime.trim().isEmpty()) {
            return false;
        }

        if (!validateTimeFormat()) {
            return false;
        }

        if (!validateDateLogic()) {
            return false;
        }

        if (!validateTimeLogic()) {
            return false;
        }


        return true;
    }

    private boolean validateTimeFormat() {
        return isValidTimeFormat(this.inTime) && isValidTimeFormat(this.outTime);
    }


    private boolean validateDateLogic() {
        Date today = new Date();
        if (this.happenDate != null && this.happenDate.after(today)) {
            long diffInDays = (this.happenDate.getTime() - today.getTime()) / (24 * 60 * 60 * 1000);
            if (diffInDays > 30) {
                return false; // Too far in the future
            }
        }

        if (this.logTime != null && this.happenDate != null) {
            return !this.logTime.before(this.happenDate);
        }

        return true;
    }


    private boolean validateTimeLogic() {
        if (this.inTime != null && this.outTime != null) {
            int inMinutes = timeToMinutes(this.inTime);
            int outMinutes = timeToMinutes(this.outTime);

            if (inMinutes == -1 || outMinutes == -1) {
                return false; // Invalid time format
            }

            return outMinutes > inMinutes;
        }
        return true;
    }


    private boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time.matches(timePattern);
    }


    private int timeToMinutes(String time) {
        if (!isValidTimeFormat(time)) {
            return -1;
        }

        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    public boolean hasRequiredFields() {
        return Objects.nonNull(this.employeeId) && !this.employeeId.trim().isEmpty() &&
                Objects.nonNull(this.userId) && !this.userId.trim().isEmpty() &&
                Objects.nonNull(this.movementType) &&
                Objects.nonNull(this.destination) && !this.destination.trim().isEmpty() &&
                Objects.nonNull(this.happenDate) &&
                Objects.nonNull(this.logTime) &&
                Objects.nonNull(this.inTime) && !this.inTime.trim().isEmpty() &&
                Objects.nonNull(this.outTime) && !this.outTime.trim().isEmpty();
    }


    public boolean isSameDayMovement() {
        if (this.inTime != null && this.outTime != null) {
            int inMinutes = timeToMinutes(this.inTime);
            int outMinutes = timeToMinutes(this.outTime);
            return outMinutes > inMinutes;
        }
        return true;
    }


    public int getMovementDurationMinutes() {
        if (this.inTime != null && this.outTime != null) {
            int inMinutes = timeToMinutes(this.inTime);
            int outMinutes = timeToMinutes(this.outTime);

            if (inMinutes != -1 && outMinutes != -1) {
                return Math.max(0, outMinutes - inMinutes);
            }
        }
        return 0;
    }


    public String getMovementStatus() {
        if (this.requestStatus != null) {
            return this.requestStatus.getDescription();
        }
        return "Unknown";
    }


    public boolean canBeEdited() {
        return this.requestStatus != RequestStatus.APPROVED &&
                this.requestStatus != RequestStatus.REJECTED &&
                this.requestStatus != RequestStatus.CANCELLED &&
                this.requestStatus != RequestStatus.EXPIRED;
    }

    public boolean canBeDeleted() {
        return this.requestStatus == RequestStatus.DRAFT ||
                this.requestStatus == RequestStatus.SUBMITTED;
    }


    public boolean validateBusinessRules() {
        int duration = getMovementDurationMinutes();
        if (duration > 24 * 60) { // More than 24 hours
            return false;
        }

        if (this.destination != null && this.destination.trim().length() < 2) {
            return false;
        }

        if (this.category != null && !this.category.trim().isEmpty()) {
            return this.category.trim().length() >= 2;
        }

        return true;
    }

    public Boolean getAccepted() {
        return requestStatus == RequestStatus.APPROVED;
    }

    public Boolean getPending() {
        return requestStatus == RequestStatus.PENDING_APPROVAL || requestStatus == RequestStatus.SUBMITTED;
    }

    public Boolean getReject() {
        return requestStatus == RequestStatus.REJECTED;
    }
}