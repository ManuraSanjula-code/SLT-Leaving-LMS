package com.slt.peotv.lmsmangmentservice.entity.Employee;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EditedBy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String firstName;
    private String lastName;
    private String sltId;
    private String employeeId;
    private String profilePic;
    private String comment;
}
