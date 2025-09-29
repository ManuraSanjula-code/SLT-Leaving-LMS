package com.slt.peotv.lmsmangmentservice.entity.Leave.types;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "leave_type")
public class LeaveTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String publicId;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = true;

    public LeaveTypeEntity() {
    }

    public LeaveTypeEntity(Long id, String name, String publicId, Boolean requiresApproval) {
        this.id = id;
        this.name = name;
        this.publicId = publicId;
        this.requiresApproval = requiresApproval != null ? requiresApproval : true;
    }

    public LeaveTypeEntity(String name, String publicId) {
        this.name = name;
        this.publicId = publicId;
        this.requiresApproval = true;
    }

    public LeaveTypeEntity(String name, String publicId, Boolean requiresApproval) {
        this.name = name;
        this.publicId = publicId;
        this.requiresApproval = requiresApproval != null ? requiresApproval : true;
    }

    public static LeaveTypeEntity create(String name, String publicId) {
        return new LeaveTypeEntity(name, publicId);
    }

    public static LeaveTypeEntity createWithoutApproval(String name, String publicId) {
        return new LeaveTypeEntity(name, publicId, false);
    }

    public static LeaveTypeEntity createWithApproval(String name, String publicId) {
        return new LeaveTypeEntity(name, publicId, true);
    }

    public static LeaveTypeEntity createWithDefaults(String name, String publicId) {
        LeaveTypeEntity entity = new LeaveTypeEntity();
        entity.setName(name);
        entity.setPublicId(publicId);
        entity.setRequiresApproval(true);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveTypeEntity that = (LeaveTypeEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(requiresApproval, that.requiresApproval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, publicId, requiresApproval);
    }

    @Override
    public String toString() {
        return "LeaveTypeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", publicId='" + publicId + '\'' +
                ", requiresApproval=" + requiresApproval +
                '}';
    }
}