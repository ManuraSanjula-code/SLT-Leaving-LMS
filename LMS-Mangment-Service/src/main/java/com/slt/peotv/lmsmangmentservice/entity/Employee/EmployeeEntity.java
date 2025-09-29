package com.slt.peotv.lmsmangmentservice.entity.Employee;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "employee_table")
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
    private String gender;
    private Date join_date;
    private Boolean roaster;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getSltId() {
        return sltId;
    }

    public void setSltId(String sltId) {
        this.sltId = sltId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getJoin_date() {
        return join_date;
    }

    public void setJoin_date(Date join_date) {
        this.join_date = join_date;
    }

    public Boolean getRoaster() {
        return roaster;
    }

    public void setRoaster(Boolean roaster) {
        this.roaster = roaster;
    }

    public EmployeeEntity() {
    }

    public EmployeeEntity(Long id, String employeeId, String publicId, String profilePic, String sltId, String firstName, String lastName, String email, String gender, Date join_date, Boolean roaster) {
        this.id = id;
        this.employeeId = employeeId;
        this.publicId = publicId;
        this.profilePic = profilePic;
        this.sltId = sltId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.join_date = join_date;
        this.roaster = roaster;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(employeeId, that.employeeId) && Objects.equals(publicId, that.publicId) && Objects.equals(profilePic, that.profilePic) && Objects.equals(sltId, that.sltId) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email) && Objects.equals(gender, that.gender) && Objects.equals(join_date, that.join_date) && Objects.equals(roaster, that.roaster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, publicId, profilePic, sltId, firstName, lastName, email, gender, join_date, roaster);
    }
}
