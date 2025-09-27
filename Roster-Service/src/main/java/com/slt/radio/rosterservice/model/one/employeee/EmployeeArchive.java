package com.slt.radio.rosterservice.model.one.employeee;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@Document(collection = "employees_archive")
public class EmployeeArchive {
    @Id
    private String id;
    private String userId;
    private String sltId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePic;
    private Collection<String> roles;
    private Collection<String> sections;
    private Collection<String> profiles;
    private Integer isSltEmp;
    private Integer isSltIntern;
    private Integer active = 1;
    private String phone;
    private String gender;
    private Integer highestRolePriority;
    private Date joiningDate;
    private Boolean roaster;

    public boolean isNew() {
        return id == null;
    }
}