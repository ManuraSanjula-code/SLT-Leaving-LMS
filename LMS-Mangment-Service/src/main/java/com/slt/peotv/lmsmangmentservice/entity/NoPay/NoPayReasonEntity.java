package com.slt.peotv.lmsmangmentservice.entity.NoPay;

import com.slt.peotv.lmsmangmentservice.entity.Enum.NoPayReason;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "no_pay_reason")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
