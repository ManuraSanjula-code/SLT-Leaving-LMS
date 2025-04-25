package com.slt.peotv.lmsmangmentservice.messaging;

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

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LMSUser lmsUser = (LMSUser) o;
        return Objects.equals(employeeId, lmsUser.employeeId) && Objects.equals(sltId, lmsUser.sltId) && Objects.equals(firstName, lmsUser.firstName) && Objects.equals(lastName, lmsUser.lastName) && Objects.equals(email, lmsUser.email) && Objects.equals(join_date, lmsUser.join_date) && Objects.equals(publicId, lmsUser.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, sltId, firstName, lastName, email, join_date, publicId);
    }

    @Override
    public String toString() {
        return "LMSUser{" +
                "employeeId='" + employeeId + '\'' +
                ", sltId='" + sltId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", join_date=" + join_date +
                ", publicId='" + publicId + '\'' +
                '}';
    }
}
