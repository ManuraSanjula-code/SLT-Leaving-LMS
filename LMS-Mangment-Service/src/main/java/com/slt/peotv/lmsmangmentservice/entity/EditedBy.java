package com.slt.peotv.lmsmangmentservice.entity;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
public class EditedBy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    private EmployeeEntity employee;
    private String comment;
}
