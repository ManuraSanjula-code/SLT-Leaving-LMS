package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import lombok.Data;
import lombok.ToString;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Data
@ToString
public class MovementReq {

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

    private Boolean isAbsent = false;
    private Boolean isUnSuccessfulAttdate = false;
    private Boolean isHalfDay = false;
    private Boolean unAuthorized = false;
    private Boolean isLate = false;
    private Boolean isLateCover = false;

    @NotNull(message = "Log time is required")

    private Date logTime;

    @NotBlank(message = "In time is required")
    private String intime = "00:00";

    @NotBlank(message = "Out time is required")
    private String outtime = "00:00";

    private String adminId;
    private String adminComment;

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

        if (Objects.isNull(this.intime) || this.intime.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.outtime) || this.outtime.trim().isEmpty()) {
            return false;
        }
        // Additional business logic validations
        if (this.happenDate != null && this.logTime != null) {
            // Ensure happenDate is not in the future
            if (this.happenDate.after(new Date())) {
                return false;
            }
        }
        // Validate time format if needed (assuming HH:mm format)
        /*if (!isValidTimeFormat(this.intime) || !isValidTimeFormat(this.outtime)) {
            return false;
        }*/

        return true;
    }

    private boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        // Simple regex for HH:mm format
        String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time.matches(timePattern);
    }

    // Utility method to check if any required field is missing
    public boolean hasRequiredFields() {
        return Objects.nonNull(this.employeeId) && !this.employeeId.trim().isEmpty() &&
                Objects.nonNull(this.userId) && !this.userId.trim().isEmpty() &&
                Objects.nonNull(this.movementType) &&
                Objects.nonNull(this.destination) && !this.destination.trim().isEmpty() &&
                Objects.nonNull(this.happenDate) &&
                Objects.nonNull(this.logTime) &&
                Objects.nonNull(this.intime) && !this.intime.trim().isEmpty() &&
                Objects.nonNull(this.outtime) && !this.outtime.trim().isEmpty();
    }
}