package com.slt.peotv.lmsmangmentservice.entity.Movement;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "movement_admins")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MovementAdminsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String movementId;
    private String userId;
    private String sltId;
    private String employeeId;
    private Date approvedDate;
    private Integer highestRolePriority;
    private Boolean isAccepted;
}
