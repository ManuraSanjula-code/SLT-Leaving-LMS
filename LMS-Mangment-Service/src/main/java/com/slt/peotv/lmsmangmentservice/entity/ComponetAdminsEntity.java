package com.slt.peotv.lmsmangmentservice.entity;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "componet_admins")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ComponetAdminsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String componetID;
    @ManyToOne
    private EmployeeEntity employee;
    private Date approvedDate;
    private Integer highestRolePriority;
    private Boolean isAccepted;
    private String profilePic;
}

