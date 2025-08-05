package com.slt.radio.rosterservice.messaging;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class LMSUser implements Serializable {
    private static final long serialVersionUID = 27363533L;

    private String employeeId;
    private String sltId;
    private String firstName;
    private String lastName;
    private String email;
    private Date join_date;
    private String publicId;
    private Boolean roaster;
    private String gender;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public Date getJoin_date() {
        return join_date;
    }

    public void setJoin_date(Date join_date) {
        this.join_date = join_date;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Boolean getRoaster() {
        return roaster;
    }

    public void setRoaster(Boolean roaster) {
        this.roaster = roaster;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LMSUser lmsUser = (LMSUser) o;
        return Objects.equals(employeeId, lmsUser.employeeId) && 
               Objects.equals(sltId, lmsUser.sltId) && 
               Objects.equals(email, lmsUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, sltId, email);
    }

    @Override
    public String toString() {
        return "LMSUser{" +
                "employeeId='" + employeeId + '\'' +
                ", sltId='" + sltId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}