package com.slt.peotv.lmsmangmentservice.entity.Movement;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import javax.persistence.*;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "movements")
public class MovementsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;

    @Column(name = "In_Time", length = 45)
    private Time inTime;

    @Column(name = "Out_Time", length = 45)
    private Time outTime;

    private String inTimeRaw;
    private String outTimeRaw;
    private String happenDateRaw;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Log_Time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;

    @Column(name = "category", length = 45)
    private String category;

    @Column(name = "Destination", length = 45)
    private String destination;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(name = "REQ_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reqDate;

    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "ATT_SYNC")
    private Integer attSync = 0;

    private Date happenDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ComponetAdminsEntity> admins;

    private Date createDate = new Date();
    private Date updateDate;

    private Boolean isEdited = false;

    public MovementsEntity() {
    }

    public MovementsEntity(Long id, String publicId, Time inTime, Time outTime, String inTimeRaw, String outTimeRaw, String happenDateRaw, String comment, Date logTime, String category, String destination, EmployeeEntity employee, Date reqDate, MovementType movementType, Integer attSync, Date happenDate, RequestStatus requestStatus, AttendanceEntity attendance, List<ComponetAdminsEntity> admins, Date createDate, Date updateDate, Boolean isEdited) {
        this.id = id;
        this.publicId = publicId;
        this.inTime = inTime;
        this.outTime = outTime;
        this.inTimeRaw = inTimeRaw;
        this.outTimeRaw = outTimeRaw;
        this.happenDateRaw = happenDateRaw;
        this.comment = comment;
        this.logTime = logTime;
        this.category = category;
        this.destination = destination;
        this.employee = employee;
        this.reqDate = reqDate;
        this.movementType = movementType;
        this.attSync = attSync != null ? attSync : 0;
        this.happenDate = happenDate;
        this.requestStatus = requestStatus;
        this.attendance = attendance;
        this.admins = admins;
        this.createDate = createDate != null ? createDate : new Date();
        this.updateDate = updateDate;
        this.isEdited = isEdited != null ? isEdited : false;
    }

    public MovementsEntity(String publicId, EmployeeEntity employee, Date happenDate, MovementType movementType) {
        this.publicId = publicId;
        this.employee = employee;
        this.happenDate = happenDate;
        this.movementType = movementType;
        this.attSync = 0;
        this.createDate = new Date();
        this.isEdited = false;
    }

    public MovementsEntity(String publicId, EmployeeEntity employee, Date happenDate, MovementType movementType, Time inTime, Time outTime) {
        this(publicId, employee, happenDate, movementType);
        this.inTime = inTime;
        this.outTime = outTime;
    }

    public static MovementsEntity create(String publicId, EmployeeEntity employee, Date happenDate, MovementType movementType) {
        return new MovementsEntity(publicId, employee, happenDate, movementType);
    }

    public static MovementsEntity createWithTimes(String publicId, EmployeeEntity employee, Date happenDate, MovementType movementType, Time inTime, Time outTime) {
        return new MovementsEntity(publicId, employee, happenDate, movementType, inTime, outTime);
    }

    public static MovementsEntity createWithDefaults(String publicId, EmployeeEntity employee, Date happenDate, MovementType movementType) {
        MovementsEntity entity = new MovementsEntity();
        entity.setPublicId(publicId);
        entity.setEmployee(employee);
        entity.setHappenDate(happenDate);
        entity.setMovementType(movementType);
        entity.setAttSync(0);
        entity.setCreateDate(new Date());
        entity.setIsEdited(false);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Time getInTime() {
        return inTime;
    }

    public void setInTime(Time inTime) {
        this.inTime = inTime;
    }

    public Time getOutTime() {
        return outTime;
    }

    public void setOutTime(Time outTime) {
        this.outTime = outTime;
    }

    public String getInTimeRaw() {
        return inTimeRaw;
    }

    public void setInTimeRaw(String inTimeRaw) {
        this.inTimeRaw = inTimeRaw;
    }

    public String getOutTimeRaw() {
        return outTimeRaw;
    }

    public void setOutTimeRaw(String outTimeRaw) {
        this.outTimeRaw = outTimeRaw;
    }

    public String getHappenDateRaw() {
        return happenDateRaw;
    }

    public void setHappenDateRaw(String happenDateRaw) {
        this.happenDateRaw = happenDateRaw;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public Date getReqDate() {
        return reqDate;
    }

    public void setReqDate(Date reqDate) {
        this.reqDate = reqDate;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getAttSync() {
        return attSync;
    }

    public void setAttSync(Integer attSync) {
        this.attSync = attSync;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }

    public List<ComponetAdminsEntity> getAdmins() {
        return admins;
    }

    public void setAdmins(List<ComponetAdminsEntity> admins) {
        this.admins = admins;
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
        MovementsEntity that = (MovementsEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(inTime, that.inTime) &&
                Objects.equals(outTime, that.outTime) &&
                Objects.equals(inTimeRaw, that.inTimeRaw) &&
                Objects.equals(outTimeRaw, that.outTimeRaw) &&
                Objects.equals(happenDateRaw, that.happenDateRaw) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(logTime, that.logTime) &&
                Objects.equals(category, that.category) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(reqDate, that.reqDate) &&
                movementType == that.movementType &&
                Objects.equals(attSync, that.attSync) &&
                Objects.equals(happenDate, that.happenDate) &&
                requestStatus == that.requestStatus &&
                Objects.equals(attendance, that.attendance) &&
                Objects.equals(admins, that.admins) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(isEdited, that.isEdited);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, inTime, outTime, inTimeRaw, outTimeRaw, happenDateRaw, comment, logTime, category, destination, employee, reqDate, movementType, attSync, happenDate, requestStatus, attendance, admins, createDate, updateDate, isEdited);
    }

    @Override
    public String toString() {
        return "MovementsEntity{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", inTime=" + inTime +
                ", outTime=" + outTime +
                ", inTimeRaw='" + inTimeRaw + '\'' +
                ", outTimeRaw='" + outTimeRaw + '\'' +
                ", happenDateRaw='" + happenDateRaw + '\'' +
                ", comment='" + comment + '\'' +
                ", logTime=" + logTime +
                ", category='" + category + '\'' +
                ", destination='" + destination + '\'' +
                ", employee=" + employee +
                ", reqDate=" + reqDate +
                ", movementType=" + movementType +
                ", attSync=" + attSync +
                ", happenDate=" + happenDate +
                ", requestStatus=" + requestStatus +
                ", attendance=" + attendance +
                ", admins=" + admins +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", isEdited=" + isEdited +
                '}';
    }
}