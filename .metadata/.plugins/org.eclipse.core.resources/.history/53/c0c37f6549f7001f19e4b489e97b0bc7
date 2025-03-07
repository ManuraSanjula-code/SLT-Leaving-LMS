package com.slt.peotv.lmsmangmentservice.entity.Movement;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import lombok.*;

@Entity
@Table(name = "movements")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;
    private String employeeID;

    @Column(name = "In_Time", length = 45)
    private String inTime;

    @Column(name = "Out_Time", length = 45)
    private String outTime;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Log_Time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;

    @Column(name = "Sup_app_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date supAppTime;

    @Column(name = "Man_App_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date manAppTime;

    @Column(name = "HOD_App_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hodAppTime;

    @Column(name = "category", length = 45)
    private String category;

    @Column(name = "Destination", length = 45)
    private String destination;

    @Column(name = "Employee_ID", length = 45)
    private String employeeId;

    @Column(name = "REQ_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reqDate;

    private String hod;
    private String supervisor;

    @Enumerated(EnumType.STRING)  // FIXED: Enum mapping
    private MovementType movementType;

    @Column(name = "ATT_SYNC")
    @Builder.Default
    private Integer attSync = 0;

    private Date happenDate; /// The Day situation happened to make a movement to resolve it

    @Builder.Default
    private Boolean isPending = false;
    @Builder.Default
    private Boolean isAccepted = false;
    @Builder.Default
    private Boolean isExpired = false;
    @Builder.Default
    private Boolean isHalfDay = false;
    @Builder.Default
    private Boolean isLate = false;
    @Builder.Default
    private Boolean isAbsent = false;
    @Builder.Default
    private Boolean isUnSuccessfulAttdate = false;
    @Builder.Default
    private Boolean isLateCover = false;
    @Builder.Default
    private Boolean unAuthorized = false;

    @OneToOne
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

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

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
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

    public Date getSupAppTime() {
        return supAppTime;
    }

    public void setSupAppTime(Date supAppTime) {
        this.supAppTime = supAppTime;
    }

    public Date getManAppTime() {
        return manAppTime;
    }

    public void setManAppTime(Date manAppTime) {
        this.manAppTime = manAppTime;
    }

    public Date getHodAppTime() {
        return hodAppTime;
    }

    public void setHodAppTime(Date hodAppTime) {
        this.hodAppTime = hodAppTime;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Date getReqDate() {
        return reqDate;
    }

    public void setReqDate(Date reqDate) {
        this.reqDate = reqDate;
    }

    public String getHod() {
        return hod;
    }

    public void setHod(String hod) {
        this.hod = hod;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
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

    public Boolean getPending() {
        return isPending;
    }

    public void setPending(Boolean pending) {
        isPending = pending;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public Boolean getHalfDay() {
        return isHalfDay;
    }

    public void setHalfDay(Boolean halfDay) {
        isHalfDay = halfDay;
    }

    public Boolean getLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        isLate = late;
    }

    public Boolean getAbsent() {
        return isAbsent;
    }

    public void setAbsent(Boolean absent) {
        isAbsent = absent;
    }

    public Boolean getUnSuccessfulAttdate() {
        return isUnSuccessfulAttdate;
    }

    public void setUnSuccessfulAttdate(Boolean unSuccessfulAttdate) {
        isUnSuccessfulAttdate = unSuccessfulAttdate;
    }

    public Boolean getLateCover() {
        return isLateCover;
    }

    public void setLateCover(Boolean lateCover) {
        isLateCover = lateCover;
    }

    public Boolean getUnAuthorized() {
        return unAuthorized;
    }

    public void setUnAuthorized(Boolean unAuthorized) {
        this.unAuthorized = unAuthorized;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }
}