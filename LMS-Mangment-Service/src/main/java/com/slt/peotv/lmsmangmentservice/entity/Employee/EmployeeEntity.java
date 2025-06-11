package com.slt.peotv.lmsmangmentservice.entity.Employee;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "employee_table")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String employeeId;
    private String publicId;
    private String profilePic;
    @Column(unique = true)
    private String sltId;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private Date join_date;
    private Boolean roaster;
}
