package com.slt.peotv.lmsmangmentservice.entity.NoPay;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "no_pay")
public class NoPayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Column(name = "submission_date", nullable = false)
    private Date submissionDate;

    @Column(name = "actual_date", nullable = false)
    private Date date;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    private Date updatedDate = new Date();

    @Column(name = "is_active")
    private Boolean isActive = true;

    public NoPayEntity() {
    }

    public NoPayEntity(Long id, String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date, String comment, Date createdDate, Date updatedDate, Boolean isActive) {
        this.id = id;
        this.publicId = publicId;
        this.employee = employee;
        this.attendance = attendance;
        this.submissionDate = submissionDate;
        this.date = date;
        this.comment = comment;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.updatedDate = updatedDate != null ? updatedDate : new Date();
        this.isActive = isActive != null ? isActive : true;
    }

    public NoPayEntity(String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date) {
        this.publicId = publicId;
        this.employee = employee;
        this.attendance = attendance;
        this.submissionDate = submissionDate;
        this.date = date;
        this.createdDate = new Date();
        this.updatedDate = new Date();
        this.isActive = true;
    }

    public NoPayEntity(String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date, String comment) {
        this(publicId, employee, attendance, submissionDate, date);
        this.comment = comment;
    }

    public static NoPayEntity create(String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date) {
        return new NoPayEntity(publicId, employee, attendance, submissionDate, date);
    }

    public static NoPayEntity createWithComment(String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date, String comment) {
        return new NoPayEntity(publicId, employee, attendance, submissionDate, date, comment);
    }

    public static NoPayEntity createWithDefaults(String publicId, EmployeeEntity employee, AttendanceEntity attendance, Date submissionDate, Date date) {
        NoPayEntity entity = new NoPayEntity();
        entity.setPublicId(publicId);
        entity.setEmployee(employee);
        entity.setAttendance(attendance);
        entity.setSubmissionDate(submissionDate);
        entity.setDate(date);
        entity.setCreatedDate(new Date());
        entity.setUpdatedDate(new Date());
        entity.setIsActive(true);
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

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoPayEntity that = (NoPayEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(employee, that.employee) &&
                Objects.equals(attendance, that.attendance) &&
                Objects.equals(submissionDate, that.submissionDate) &&
                Objects.equals(date, that.date) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(updatedDate, that.updatedDate) &&
                Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, employee, attendance, submissionDate, date, comment, createdDate, updatedDate, isActive);
    }

    @Override
    public String toString() {
        return "NoPayEntity{" +
                "id=" + id +
                ", publicId='" + publicId + '\'' +
                ", employee=" + employee +
                ", attendance=" + attendance +
                ", submissionDate=" + submissionDate +
                ", date=" + date +
                ", comment='" + comment + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", isActive=" + isActive +
                '}';
    }
}