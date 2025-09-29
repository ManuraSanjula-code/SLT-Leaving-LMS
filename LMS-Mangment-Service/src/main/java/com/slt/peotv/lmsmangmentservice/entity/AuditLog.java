package com.slt.peotv.lmsmangmentservice.entity;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AuditAction;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        })
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private EmployeeEntity employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_identifier")
    private String entityIdentifier;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "timestamp", nullable = false)
    private Date timestamp = new Date();

    public AuditLog() {
    }

    public AuditLog(Long id, EmployeeEntity employee, AuditAction action, String entityType, Long entityId, String entityIdentifier, String comment, Date timestamp) {
        this.id = id;
        this.employee = employee;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityIdentifier = entityIdentifier;
        this.comment = comment;
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    public AuditLog(EmployeeEntity employee, AuditAction action, String entityType, Long entityId) {
        this.employee = employee;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = new Date();
    }

    public AuditLog(EmployeeEntity employee, AuditAction action, String entityType, Long entityId, String entityIdentifier) {
        this(employee, action, entityType, entityId);
        this.entityIdentifier = entityIdentifier;
    }

    public AuditLog(EmployeeEntity employee, AuditAction action, String entityType, Long entityId, String entityIdentifier, String comment) {
        this(employee, action, entityType, entityId, entityIdentifier);
        this.comment = comment;
    }

    public static AuditLog create(EmployeeEntity employee, AuditAction action, String entityType, Long entityId) {
        return new AuditLog(employee, action, entityType, entityId);
    }

    public static AuditLog createWithIdentifier(EmployeeEntity employee, AuditAction action, String entityType, Long entityId, String entityIdentifier) {
        return new AuditLog(employee, action, entityType, entityId, entityIdentifier);
    }

    public static AuditLog createWithComment(EmployeeEntity employee, AuditAction action, String entityType, Long entityId, String entityIdentifier, String comment) {
        return new AuditLog(employee, action, entityType, entityId, entityIdentifier, comment);
    }

    public static AuditLog createWithDefaults(EmployeeEntity employee, AuditAction action, String entityType, Long entityId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEmployee(employee);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setTimestamp(new Date());
        return auditLog;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id) &&
                Objects.equals(employee, auditLog.employee) &&
                action == auditLog.action &&
                Objects.equals(entityType, auditLog.entityType) &&
                Objects.equals(entityId, auditLog.entityId) &&
                Objects.equals(entityIdentifier, auditLog.entityIdentifier) &&
                Objects.equals(comment, auditLog.comment) &&
                Objects.equals(timestamp, auditLog.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employee, action, entityType, entityId, entityIdentifier, comment, timestamp);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", employee=" + employee +
                ", action=" + action +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", comment='" + comment + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}