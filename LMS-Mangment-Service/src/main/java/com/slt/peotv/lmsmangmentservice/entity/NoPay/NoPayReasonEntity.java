package com.slt.peotv.lmsmangmentservice.entity.NoPay;

import com.slt.peotv.lmsmangmentservice.entity.Enum.NoPayReason;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "no_pay_reason")
public class NoPayReasonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_pay_id", nullable = false)
    private NoPayEntity noPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private NoPayReason reason;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Column(name = "is_active")
    private Boolean isActive = true;

    public NoPayReasonEntity() {
    }

    public NoPayReasonEntity(Long id, NoPayEntity noPay, NoPayReason reason, Date createdDate, Boolean isActive) {
        this.id = id;
        this.noPay = noPay;
        this.reason = reason;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.isActive = isActive != null ? isActive : true;
    }

    public NoPayReasonEntity(NoPayEntity noPay, NoPayReason reason) {
        this.noPay = noPay;
        this.reason = reason;
        this.createdDate = new Date();
        this.isActive = true;
    }

    public NoPayReasonEntity(NoPayEntity noPay, NoPayReason reason, Date createdDate) {
        this.noPay = noPay;
        this.reason = reason;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.isActive = true;
    }

    public static NoPayReasonEntity create(NoPayEntity noPay, NoPayReason reason) {
        return new NoPayReasonEntity(noPay, reason);
    }

    public static NoPayReasonEntity createInactive(NoPayEntity noPay, NoPayReason reason) {
        NoPayReasonEntity entity = new NoPayReasonEntity(noPay, reason);
        entity.setIsActive(false);
        return entity;
    }

    public static NoPayReasonEntity createWithDefaults(NoPayEntity noPay, NoPayReason reason) {
        NoPayReasonEntity entity = new NoPayReasonEntity();
        entity.setNoPay(noPay);
        entity.setReason(reason);
        entity.setCreatedDate(new Date());
        entity.setIsActive(true);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NoPayEntity getNoPay() {
        return noPay;
    }

    public void setNoPay(NoPayEntity noPay) {
        this.noPay = noPay;
    }

    public NoPayReason getReason() {
        return reason;
    }

    public void setReason(NoPayReason reason) {
        this.reason = reason;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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
        NoPayReasonEntity that = (NoPayReasonEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(noPay, that.noPay) &&
                reason == that.reason &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, noPay, reason, createdDate, isActive);
    }

    @Override
    public String toString() {
        return "NoPayReasonEntity{" +
                "id=" + id +
                ", noPay=" + noPay +
                ", reason=" + reason +
                ", createdDate=" + createdDate +
                ", isActive=" + isActive +
                '}';
    }
}