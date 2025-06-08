package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.model.dto.EditedByDTO;
import com.slt.peotv.lmsmangmentservice.model.dto.LeaveTra;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LeaveReq {

    // Basic identifiers (usually not required for create requests, but useful for updates)
    private String publicId;
    private Long id;
    private String employeeID;
    private Date submitDate;
    private Date createDate;
    private Date updateDate;

    @NotNull(message = "From date is required")
    private Date fromDate;

    @NotNull(message = "To date is required")
    private Date toDate;

    @NotBlank(message = "Leave type is required")
    private String leaveType;

    private String description;

    @NotBlank(message = "User ID is required")
    private String userId;

    @Min(value = 0, message = "Number of days must be at least 0")
    private Long numOfDays; // Changed to Double to support 0.5 for half days

    @NotNull(message = "Happen date is required")
    private Date happenDate;

    private Integer isNoPay = 0;

    // Boolean flags with default values - using exact field names that frontend expects
    private Boolean halfDay = false;          // Frontend: isHalfDay -> Backend: halfDay
    private Boolean fullDay = false;          // Frontend: isFullDay -> Backend: fullDay
    private Boolean unSuccessful = false;
    private Boolean unauthorized = false;     // Frontend: isUnauthorized -> Backend: unauthorized
    private Boolean manualRequest = false;    // Frontend: isManualRequest -> Backend: manualRequest
    private Boolean absent = false;           // Frontend: isAbsent -> Backend: absent
    private Boolean lateCover = false;        // Frontend: isLateCover -> Backend: lateCover
    private Boolean late = false;             // Frontend: isLate -> Backend: late
    private Boolean edited = false;           // Frontend: isEdited -> Backend: edited
    private Boolean reject = false;           // Frontend: isReject -> Backend: reject
    private Boolean canceled = false;         // Frontend: isCanceled -> Backend: canceled
    private Boolean accepted = false;         // Frontend: isAccepted -> Backend: accepted
    private Boolean pending = false;          // Frontend: isPending -> Backend: pending
    private Boolean short_Leave = false;      // Frontend: isShort_Leave -> Backend: short_Leave
    private Boolean notUsed = false;          // Frontend: notUsed -> Backend: notUsed

    // Admin fields
    private String adminId;
    private String adminComment;

    // Complex object fields (usually for updates or detailed requests)
    private List<LeaveTra> adminsTra;
    private List<EditedByDTO> editedByDTOs;

    // Getters and Setters
    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getNumOfDays() {
        return numOfDays;
    }

    public void setNumOfDays(Long numOfDays) {
        this.numOfDays = numOfDays;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public Integer getIsNoPay() {
        return isNoPay;
    }

    public void setIsNoPay(Integer isNoPay) {
        this.isNoPay = isNoPay;
    }

    // Updated getters and setters with correct field names
    public Boolean getHalfDay() {
        return halfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        this.halfDay = halfDay;
    }

    public Boolean getFullDay() {
        return fullDay;
    }

    public void setFullDay(Boolean fullDay) {
        this.fullDay = fullDay;
    }

    public Boolean getUnSuccessful() {
        return unSuccessful;
    }

    public void setUnSuccessful(Boolean unSuccessful) {
        this.unSuccessful = unSuccessful;
    }

    public Boolean getUnauthorized() {
        return unauthorized;
    }

    public void setUnauthorized(Boolean unauthorized) {
        this.unauthorized = unauthorized;
    }

    public Boolean getManualRequest() {
        return manualRequest;
    }

    public void setManualRequest(Boolean manualRequest) {
        this.manualRequest = manualRequest;
    }

    public Boolean getAbsent() {
        return absent;
    }

    public void setAbsent(Boolean absent) {
        this.absent = absent;
    }

    public Boolean getLateCover() {
        return lateCover;
    }

    public void setLateCover(Boolean lateCover) {
        this.lateCover = lateCover;
    }

    public Boolean getLate() {
        return late;
    }

    public void setLate(Boolean late) {
        this.late = late;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public Boolean getReject() {
        return reject;
    }

    public void setReject(Boolean reject) {
        this.reject = reject;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Boolean getShort_Leave() {
        return short_Leave;
    }

    public void setShort_Leave(Boolean short_Leave) {
        this.short_Leave = short_Leave;
    }

    public Boolean getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(Boolean notUsed) {
        this.notUsed = notUsed;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public List<LeaveTra> getAdminsTra() {
        return adminsTra;
    }

    public void setAdminsTra(List<LeaveTra> adminsTra) {
        this.adminsTra = adminsTra;
    }

    public List<EditedByDTO> getEditedByDTOs() {
        return editedByDTOs;
    }

    public void setEditedByDTOs(List<EditedByDTO> editedByDTOs) {
        this.editedByDTOs = editedByDTOs;
    }

    /**
     * Validates the leave request
     * @return true if valid, false otherwise
     */
    public boolean validateLeaveReq() {
        // Basic null and empty checks
        if (Objects.isNull(this.fromDate)) {
            return false;
        }

        if (Objects.isNull(this.toDate)) {
            return false;
        }

        if (Objects.isNull(this.leaveType) || this.leaveType.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.userId) || this.userId.trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(this.happenDate)) {
            return false;
        }

        if (Objects.isNull(this.numOfDays) || this.numOfDays <= 0) {
            return false;
        }

        // Business logic validations
        if (!validateDateRange()) {
            return false;
        }

        if (!validateDayTypeConsistency()) {
            return false;
        }

        if (!validateNumberOfDays()) {
            return false;
        }

        if (!validateMutuallyExclusiveFlags()) {
            return false;
        }

        return true;
    }

    /**
     * Validates that fromDate is before or equal to toDate
     */
    private boolean validateDateRange() {
        if (this.fromDate != null && this.toDate != null) {
            return !this.fromDate.after(this.toDate);
        }
        return true;
    }

    /**
     * Validates that half-day and full-day flags are not both true
     */
    private boolean validateDayTypeConsistency() {
        if (Boolean.TRUE.equals(this.halfDay) && Boolean.TRUE.equals(this.fullDay)) {
            return false; // Cannot be both half-day and full-day
        }

        // If numOfDays is 1, it should be either half-day or full-day
        if (Objects.nonNull(this.numOfDays) && this.numOfDays == 1) {
            return Boolean.TRUE.equals(this.halfDay) || Boolean.TRUE.equals(this.fullDay);
        }

        // If numOfDays > 1, it should be full-day and not half-day
        if (Objects.nonNull(this.numOfDays) && this.numOfDays > 1) {
            return Boolean.TRUE.equals(this.fullDay) && !Boolean.TRUE.equals(this.halfDay);
        }

        return true;
    }

    /**
     * Validates number of days consistency with date range
     */
    private boolean validateNumberOfDays() {
        if (this.fromDate != null && this.toDate != null && this.numOfDays != null) {
            long daysDiff = calculateDaysBetween(this.fromDate, this.toDate);

            // For half-day leaves, numOfDays should be 0.5 or 1
            if (Boolean.TRUE.equals(this.halfDay)) {
                return daysDiff <= 1 && this.numOfDays <= 1;
            }

            // For full-day leaves, numOfDays should be within reasonable range of date difference
            if (Boolean.TRUE.equals(this.fullDay)) {
                return this.numOfDays >= daysDiff && this.numOfDays <= (daysDiff + 2); // Allow some flexibility
            }
        }

        return this.numOfDays != null && this.numOfDays > 0;
    }

    /**
     * Validates mutually exclusive status flags
     */
    private boolean validateMutuallyExclusiveFlags() {
        int statusCount = 0;

        if (Boolean.TRUE.equals(this.accepted)) statusCount++;
        if (Boolean.TRUE.equals(this.reject)) statusCount++;
        if (Boolean.TRUE.equals(this.pending)) statusCount++;
        if (Boolean.TRUE.equals(this.canceled)) statusCount++;

        // Only one status should be true at a time
        if (statusCount > 1) {
            return false;
        }

        // Cannot be both authorized and unauthorized
        if (Boolean.TRUE.equals(this.unauthorized) && Boolean.TRUE.equals(this.accepted)) {
            return false;
        }

        // Cannot be both absent and present for the same leave
        if (Boolean.TRUE.equals(this.absent) && Boolean.TRUE.equals(this.accepted)) {
            return false;
        }

        return true;
    }

    /**
     * Calculate days between two dates
     */
    private long calculateDaysBetween(Date startDate, Date endDate) {
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
        return diffInMillies / (24 * 60 * 60 * 1000) + 1; // +1 to include both start and end dates
    }

    /**
     * Check if all required fields are present
     */
    public boolean hasRequiredFields() {
        return Objects.nonNull(this.fromDate) &&
                Objects.nonNull(this.toDate) &&
                Objects.nonNull(this.leaveType) && !this.leaveType.trim().isEmpty() &&
                Objects.nonNull(this.userId) && !this.userId.trim().isEmpty() &&
                Objects.nonNull(this.happenDate) &&
                Objects.nonNull(this.numOfDays) && this.numOfDays > 0;
    }

    /**
     * Utility method to check if this is a single day leave
     */
    public boolean isSingleDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays == 1;
    }

    /**
     * Utility method to check if this is a half day leave
     */
    public boolean isHalfDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays == 0.5;
    }

    /**
     * Utility method to check if this is a multi-day leave
     */
    public boolean isMultiDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays > 1;
    }

    /**
     * Get the leave status as a string
     */
    public String getLeaveStatus() {
        if (Boolean.TRUE.equals(this.reject)) return "Rejected";
        if (Boolean.TRUE.equals(this.pending)) return "Pending";
        if (Boolean.TRUE.equals(this.accepted)) return "Approved";
        if (Boolean.TRUE.equals(this.canceled)) return "Canceled";
        if (Boolean.TRUE.equals(this.late) && !Boolean.TRUE.equals(this.pending) && !Boolean.TRUE.equals(this.accepted))
            return "Recorded Late";
        if (!Boolean.TRUE.equals(this.fullDay) && !Boolean.TRUE.equals(this.halfDay) &&
                !Boolean.TRUE.equals(this.late) && !Boolean.TRUE.equals(this.pending) &&
                !Boolean.TRUE.equals(this.accepted)) return "Recorded Absent";
        return "Processed";
    }

    /**
     * Get the leave type as a string
     */
    public String getLeaveTypeString() {
        if (Boolean.TRUE.equals(this.fullDay)) return "Full Day Leave";
        if (Boolean.TRUE.equals(this.halfDay)) return "Half Day Leave";
        if (Boolean.TRUE.equals(this.absent)) return "Absence";
        if (Boolean.TRUE.equals(this.late)) return "Late Arrival";
        return this.leaveType != null ? this.leaveType : "Regular Leave";
    }

    /**
     * Check if the leave request can be edited
     */
    public boolean canBeEdited() {
        return !Boolean.TRUE.equals(this.accepted) &&
                !Boolean.TRUE.equals(this.reject) &&
                !Boolean.TRUE.equals(this.canceled);
    }

    /**
     * Check if the leave request can be deleted
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(this.accepted) &&
                !Boolean.TRUE.equals(this.reject) &&
                !Boolean.TRUE.equals(this.canceled);
    }

    @Override
    public String toString() {
        return "LeaveReq{" +
                "publicId='" + publicId + '\'' +
                ", id=" + id +
                ", employeeID='" + employeeID + '\'' +
                ", submitDate=" + submitDate +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", leaveType='" + leaveType + '\'' +
                ", description='" + description + '\'' +
                ", userId='" + userId + '\'' +
                ", numOfDays=" + numOfDays +
                ", happenDate=" + happenDate +
                ", isNoPay=" + isNoPay +
                ", halfDay=" + halfDay +
                ", fullDay=" + fullDay +
                ", unSuccessful=" + unSuccessful +
                ", unauthorized=" + unauthorized +
                ", manualRequest=" + manualRequest +
                ", absent=" + absent +
                ", lateCover=" + lateCover +
                ", late=" + late +
                ", edited=" + edited +
                ", reject=" + reject +
                ", canceled=" + canceled +
                ", accepted=" + accepted +
                ", pending=" + pending +
                ", short_Leave=" + short_Leave +
                ", notUsed=" + notUsed +
                ", adminId='" + adminId + '\'' +
                ", adminComment='" + adminComment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveReq leaveReq = (LeaveReq) o;
        return Objects.equals(publicId, leaveReq.publicId) &&
                Objects.equals(id, leaveReq.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, id);
    }
}