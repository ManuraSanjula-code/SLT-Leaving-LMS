package com.slt.peotv.lmsmangmentservice.entity;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        })
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
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

    @Builder.Default
    @Column(name = "timestamp", nullable = false)
    private Date timestamp = new Date();
}
