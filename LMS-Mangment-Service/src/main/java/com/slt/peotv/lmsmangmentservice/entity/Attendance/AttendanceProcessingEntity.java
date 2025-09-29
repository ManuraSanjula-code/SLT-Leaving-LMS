package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import com.slt.peotv.lmsmangmentservice.entity.Enum.ProcessingFlag;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "attendance_processing")
public class AttendanceProcessingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_flag", nullable = false)
    private ProcessingFlag processingFlag;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }

    public ProcessingFlag getProcessingFlag() {
        return processingFlag;
    }

    public void setProcessingFlag(ProcessingFlag processingFlag) {
        this.processingFlag = processingFlag;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public AttendanceProcessingEntity() {
    }

    public AttendanceProcessingEntity(Long id, AttendanceEntity attendance, ProcessingFlag processingFlag, Date createdDate, Boolean isActive) {
        this.id = id;
        this.attendance = attendance;
        this.processingFlag = processingFlag;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.isActive = isActive != null ? isActive : true;
    }

    public AttendanceProcessingEntity(AttendanceEntity attendance, ProcessingFlag processingFlag) {
        this.attendance = attendance;
        this.processingFlag = processingFlag;
        this.createdDate = new Date();
        this.isActive = true;
    }

    public AttendanceProcessingEntity(AttendanceEntity attendance, ProcessingFlag processingFlag, Date createdDate, Boolean isActive) {
        this.attendance = attendance;
        this.processingFlag = processingFlag;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.isActive = isActive != null ? isActive : true;
    }

    public static AttendanceProcessingEntity create(AttendanceEntity attendance, ProcessingFlag processingFlag) {
        return new AttendanceProcessingEntity(attendance, processingFlag);
    }

    public static AttendanceProcessingEntity createWithDefaults(AttendanceEntity attendance, ProcessingFlag processingFlag) {
        AttendanceProcessingEntity entity = new AttendanceProcessingEntity();
        entity.setAttendance(attendance);
        entity.setProcessingFlag(processingFlag);
        entity.setCreatedDate(new Date());
        entity.setActive(true);
        return entity;
    }

    public static AttendanceProcessingEntity createInactive(AttendanceEntity attendance, ProcessingFlag processingFlag) {
        AttendanceProcessingEntity entity = new AttendanceProcessingEntity(attendance, processingFlag);
        entity.setActive(false);
        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceProcessingEntity that = (AttendanceProcessingEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(attendance, that.attendance) && processingFlag == that.processingFlag && Objects.equals(createdDate, that.createdDate) && Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attendance, processingFlag, createdDate, isActive);
    }
}