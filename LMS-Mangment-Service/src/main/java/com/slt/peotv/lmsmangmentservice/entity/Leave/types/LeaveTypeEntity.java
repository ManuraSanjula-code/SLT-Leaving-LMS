package com.slt.peotv.lmsmangmentservice.entity.Leave.types;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_type")
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LeaveTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String publicId;
    @Builder.Default
    @Column(name = "requires_approval")
    private Boolean requiresApproval = true;
}
