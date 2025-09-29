package com.slt.peotv.lmsmangmentservice.entity.Leave;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.ComponentBehavior;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "leave_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = "publicId", name = "UK_leave_public_id"))
public class LeaveEntity {

    @Column(nullable = false,unique = true)
    public String publicId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "from_date", nullable = false)
    private Date fromDate;

    @Column(name = "to_date", nullable = false)
    private Date toDate;

    @ManyToOne
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", foreignKey = @ForeignKey(name = "FK_leave_type"))
    private LeaveTypeEntity leaveType;

    @Column(name = "num_of_days")
    private Long numOfDays;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_behavior")
    private ComponentBehavior componentBehavior;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    private Boolean notUsed = false;

    private Boolean isManualRequest = false;

    private Date happenDate;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ComponetAdminsEntity> admins;

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    private Date createDate = new Date();
    private Date updateDate;

    private Boolean isEdited = false;

    public LeaveEntity() {
    }

    public LeaveEntity(String publicId, Long id, Date submitDate, Date fromDate, Date toDate, EmployeeEntity employee, LeaveTypeEntity leaveType, Long numOfDays, String description, ComponentBehavior componentBehavior, RequestStatus requestStatus, Boolean notUsed, Boolean isManualRequest, Date happenDate, List<ComponetAdminsEntity> admins, AttendanceEntity attendance, Date createDate, Date updateDate, Boolean isEdited) {
        this.publicId = publicId;
        this.id = id;
        this.submitDate = submitDate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.employee = employee;
        this.leaveType = leaveType;
        this.numOfDays = numOfDays;
        this.description = description;
        this.componentBehavior = componentBehavior;
        this.requestStatus = requestStatus;
        this.notUsed = notUsed != null ? notUsed : false;
        this.isManualRequest = isManualRequest != null ? isManualRequest : false;
        this.happenDate = happenDate;
        this.admins = admins;
        this.attendance = attendance;
        this.createDate = createDate != null ? createDate : new Date();
        this.updateDate = updateDate;
        this.isEdited = isEdited != null ? isEdited : false;
    }

    public LeaveEntity(String publicId, Date submitDate, Date fromDate, Date toDate, EmployeeEntity employee, LeaveTypeEntity leaveType) {
        this.publicId = publicId;
        this.submitDate = submitDate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.employee = employee;
        this.leaveType = leaveType;
        this.notUsed = false;
        this.isManualRequest = false;
        this.createDate = new Date();
        this.isEdited = false;
    }

    public static LeaveEntity create(String publicId, Date submitDate, Date fromDate, Date toDate, EmployeeEntity employee, LeaveTypeEntity leaveType) {
        return new LeaveEntity(publicId, submitDate, fromDate, toDate, employee, leaveType);
    }

    public static LeaveEntity createManual(String publicId, Date submitDate, Date fromDate, Date toDate, EmployeeEntity employee, LeaveTypeEntity leaveType) {
        LeaveEntity entity = new LeaveEntity(publicId, submitDate, fromDate, toDate, employee, leaveType);
        entity.setIsManualRequest(true);
        return entity;
    }

    public static LeaveEntity createWithDefaults(String publicId, Date submitDate, Date fromDate, Date toDate, EmployeeEntity employee, LeaveTypeEntity leaveType) {
        LeaveEntity entity = new LeaveEntity();
        entity.setPublicId(publicId);
        entity.setSubmitDate(submitDate);
        entity.setFromDate(fromDate);
        entity.setToDate(toDate);
        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setNotUsed(false);
        entity.setIsManualRequest(false);
        entity.setCreateDate(new Date());
        entity.setIsEdited(false);
        return entity;
    }

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

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
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

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public LeaveTypeEntity getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveTypeEntity leaveType) {
        this.leaveType = leaveType;
    }

    public Long getNumOfDays() {
        return numOfDays;
    }

    public void setNumOfDays(Long numOfDays) {
        this.numOfDays = numOfDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public List<ComponetAdminsEntity> getAdmins() {
        return admins;
    }

    public void setAdmins(List<ComponetAdminsEntity> admins) {
        this.admins = admins;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
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

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveEntity that = (LeaveEntity) o;
        return Objects.equals(publicId, that.publicId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(submitDate, that.submitDate) &&
                Objects.equals(fromDate, that.fromDate) &&
                Objects.equals(toDate, that.toDate) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(leaveType, that.leaveType) &&
                Objects.equals(numOfDays, that.numOfDays) &&
                Objects.equals(description, that.description) &&
                componentBehavior == that.componentBehavior &&
                requestStatus == that.requestStatus &&
                Objects.equals(notUsed, that.notUsed) &&
                Objects.equals(isManualRequest, that.isManualRequest) &&
                Objects.equals(happenDate, that.happenDate) &&
                Objects.equals(admins, that.admins) &&
                Objects.equals(attendance, that.attendance) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(isEdited, that.isEdited);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, id, submitDate, fromDate, toDate, employee, leaveType, numOfDays, description, componentBehavior, requestStatus, notUsed, isManualRequest, happenDate, admins, attendance, createDate, updateDate, isEdited);
    }

    @Override
    public String toString() {
        return "LeaveEntity{" +
                "publicId='" + publicId + '\'' +
                ", id=" + id +
                ", submitDate=" + submitDate +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", employee=" + employee +
                ", leaveType=" + leaveType +
                ", numOfDays=" + numOfDays +
                ", description='" + description + '\'' +
                ", componentBehavior=" + componentBehavior +
                ", requestStatus=" + requestStatus +
                ", notUsed=" + notUsed +
                ", isManualRequest=" + isManualRequest +
                ", happenDate=" + happenDate +
                ", admins=" + admins +
                ", attendance=" + attendance +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", isEdited=" + isEdited +
                '}';
    }
}