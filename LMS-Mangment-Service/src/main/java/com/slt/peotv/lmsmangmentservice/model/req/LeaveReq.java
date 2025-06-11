package com.slt.peotv.lmsmangmentservice.model.req;

import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

import java.util.Date;
import java.util.Objects;

public class LeaveReq {

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

    @NotNull(message = "Number of days is required")
    @Min(value = 1, message = "Number of days must be at least 1 (half-day)")
    private Long numOfDays;

    @NotNull(message = "Happen date is required")
    private Date happenDate;

    @NotNull(message = "Component behavior is required")
    private ComponentBehavior componentBehavior;

    private RequestStatus requestStatus = RequestStatus.DRAFT;

    private Boolean notUsed = false;
    private Boolean isManualRequest = false;
    private Boolean isEdited = false;


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


    public double getActualDays() {
        return this.numOfDays != null ? this.numOfDays / 2.0 : 0.0;
    }


    public void setActualDays(double actualDays) {
        this.numOfDays = (long) (actualDays * 2);
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }


    public ComponentBehavior getComponentBehavior() {
        return componentBehavior;
    }

    public void setComponentBehavior(ComponentBehavior componentBehavior) {
        this.componentBehavior = componentBehavior;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Boolean getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(Boolean notUsed) {
        this.notUsed = notUsed;
    }

    public Boolean getIsManualRequest() {
        return isManualRequest;
    }

    public void setIsManualRequest(Boolean isManualRequest) {
        this.isManualRequest = isManualRequest;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
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

        if (Objects.isNull(this.numOfDays) || this.numOfDays < 1) {
            return false;
        }

        if (Objects.isNull(this.componentBehavior)) {
            return false;
        }

        // Business logic validations
        if (!validateDateRange()) {
            return false;
        }

        if (!validateComponentBehaviorConsistency()) {
            return false;
        }

        if (!validateNumberOfDays()) {
            return false;
        }

        if (!validateRequestStatus()) {
            return false;
        }

        if (!validateHappenDateLogic()) {
            return false;
        }

        return true;
    }


    private boolean validateDateRange() {
        if (this.fromDate != null && this.toDate != null) {
            return !this.fromDate.after(this.toDate);
        }
        return true;
    }


    private boolean validateComponentBehaviorConsistency() {
        if (this.componentBehavior == null || this.numOfDays == null) {
            return false;
        }

        switch (this.componentBehavior) {
            case HALF_DAY:
                return this.numOfDays == 1 && isSameDayLeave();

            case FULL_DAY:
                return this.numOfDays >= 2 && this.numOfDays % 2 == 0;

            case SHORT_LEAVE:
                return this.numOfDays == 1;

            case LATE:
            case LATE_COVER:
            case ABSENT:
            case UNSUCCESSFUL:
            case UNAUTHORIZED:
                return this.numOfDays >= 1 && this.numOfDays <= 2;

            default:
                return true;
        }
    }


    private boolean validateNumberOfDays() {
        if (this.fromDate != null && this.toDate != null && this.numOfDays != null) {
            long daysDiff = calculateDaysBetween(this.fromDate, this.toDate);

            if (daysDiff == 1) {
                return this.numOfDays <= 2; // Max 1 full day (2 units)
            }

            if (daysDiff > 1) {
                long expectedMinUnits = daysDiff * 2;
                long expectedMaxUnits = expectedMinUnits + 1;
                return this.numOfDays >= expectedMinUnits && this.numOfDays <= expectedMaxUnits;
            }
        }

        return this.numOfDays != null && this.numOfDays >= 1;
    }

    private boolean validateRequestStatus() {
        if (this.requestStatus == null) {
            return false;
        }

        if (this.requestStatus == RequestStatus.CANCELLED) {
            return true;
        }

        return true;
    }


    private boolean validateHappenDateLogic() {
        if (this.happenDate == null) {
            return false;
        }

        Date today = new Date();
        if (this.componentBehavior == ComponentBehavior.LATE ||
                this.componentBehavior == ComponentBehavior.ABSENT ||
                this.componentBehavior == ComponentBehavior.UNSUCCESSFUL) {
            return !this.happenDate.after(today);
        }

        if (this.fromDate != null && this.toDate != null) {
            return !this.happenDate.before(this.fromDate) &&
                    !this.happenDate.after(this.toDate);
        }

        return true;
    }


    private long calculateDaysBetween(Date startDate, Date endDate) {
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
        return diffInMillies / (24 * 60 * 60 * 1000) + 1; // +1 to include both start and end dates
    }

    public boolean hasRequiredFields() {
        return Objects.nonNull(this.fromDate) &&
                Objects.nonNull(this.toDate) &&
                Objects.nonNull(this.leaveType) && !this.leaveType.trim().isEmpty() &&
                Objects.nonNull(this.userId) && !this.userId.trim().isEmpty() &&
                Objects.nonNull(this.happenDate) &&
                Objects.nonNull(this.numOfDays) && this.numOfDays >= 1 &&
                Objects.nonNull(this.componentBehavior);
    }


    public boolean isSingleDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays == 2; // 2 units = 1 full day
    }


    public boolean isHalfDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays == 1; // 1 unit = 0.5 days
    }


    public boolean isMultiDayLeave() {
        return Objects.nonNull(this.numOfDays) && this.numOfDays > 2; // More than 1 full day
    }


    public boolean isSameDayLeave() {
        if (this.fromDate != null && this.toDate != null) {
            return calculateDaysBetween(this.fromDate, this.toDate) == 1;
        }
        return false;
    }


    public String getLeaveStatus() {
        if (this.requestStatus != null) {
            return this.requestStatus.getDescription();
        }
        return "Unknown";
    }


    public String getComponentBehaviorString() {
        if (this.componentBehavior != null) {
            return this.componentBehavior.getDisplayName();
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


    public boolean canBeCancelled() {
        return this.requestStatus == RequestStatus.SUBMITTED ||
                this.requestStatus == RequestStatus.PENDING_APPROVAL;
    }


    public boolean requiresAdminApproval() {
        return this.componentBehavior == ComponentBehavior.FULL_DAY ||
                this.componentBehavior == ComponentBehavior.HALF_DAY ||
                (this.numOfDays != null && this.numOfDays >= 2); // 1 full day or more
    }


    public boolean validateBusinessRules() {
        // Cannot apply for leave in the past (except for certain behaviors)
        Date today = new Date();
        if (this.fromDate != null && this.fromDate.before(today)) {
            if (this.componentBehavior != ComponentBehavior.LATE &&
                    this.componentBehavior != ComponentBehavior.ABSENT &&
                    this.componentBehavior != ComponentBehavior.UNSUCCESSFUL) {
                return false;
            }
        }

        return true;
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
                ", componentBehavior=" + componentBehavior +
                ", requestStatus=" + requestStatus +
                ", notUsed=" + notUsed +
                ", isManualRequest=" + isManualRequest +
                ", isEdited=" + isEdited +
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