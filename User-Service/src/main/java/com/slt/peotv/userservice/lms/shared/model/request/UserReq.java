package com.slt.peotv.userservice.lms.shared.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class UserReq {
    private long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeId;
    private String sltId;
    private String password;
    private String encryptedPassword;
    private String profilePic;
    private String emailVerificationToken;
    private Boolean emailVerificationStatus = false;
    private List<AddressDTO> addresses;
    private Collection<String> roles;
    @JsonProperty("Authorities")
    private Collection<String> Authorities;
    private Collection<String> sections;
    private Collection<String> profiles;
    private Integer isSltEmp;
    private Integer isSltIntern;
    private Integer active = 1;
    private String phone;
    private String gender;
    private String hod;
    private String supervisor;
    private String other;
    private Boolean roaster;
    private List<String> deleteAddresses;
    private Date joiningDate;

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public Boolean getEmailVerificationStatus() {
        return emailVerificationStatus;
    }

    public void setEmailVerificationStatus(Boolean emailVerificationStatus) {
        this.emailVerificationStatus = emailVerificationStatus;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public Collection<String> getAuthorities() {
        return Authorities;
    }

    public void setAuthorities(Collection<String> authorities) {
        Authorities = authorities;
    }

    public Collection<String> getSections() {
        return sections;
    }

    public void setSections(Collection<String> sections) {
        this.sections = sections;
    }

    public Collection<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(Collection<String> profiles) {
        this.profiles = profiles;
    }

    public Integer getIsSltEmp() {
        return isSltEmp;
    }

    public void setIsSltEmp(Integer isSltEmp) {
        this.isSltEmp = isSltEmp;
    }

    public Integer getIsSltIntern() {
        return isSltIntern;
    }

    public void setIsSltIntern(Integer isSltIntern) {
        this.isSltIntern = isSltIntern;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHod() {
        return hod;
    }

    public void setHod(String hod) {
        this.hod = hod;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Boolean getRoaster() {
        return roaster;
    }

    public void setRoaster(Boolean roaster) {
        this.roaster = roaster;
    }

    public List<String> getDeleteAddresses() {
        return deleteAddresses;
    }

    public void setDeleteAddresses(List<String> deleteAddresses) {
        this.deleteAddresses = deleteAddresses;
    }

    @Override
    public String toString() {
        return "UserReq{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", sltId='" + sltId + '\'' +
                ", password='" + password + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", emailVerificationToken='" + emailVerificationToken + '\'' +
                ", emailVerificationStatus=" + emailVerificationStatus +
                ", addresses=" + addresses +
                ", roles=" + roles +
                ", Authorities=" + Authorities +
                ", sections=" + sections +
                ", profiles=" + profiles +
                ", isSltEmp=" + isSltEmp +
                ", isSltIntern=" + isSltIntern +
                ", active=" + active +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", hod='" + hod + '\'' +
                ", supervisor='" + supervisor + '\'' +
                ", other='" + other + '\'' +
                ", roaster=" + roaster +
                ", deleteAddresses=" + deleteAddresses +
                ", joiningDate=" + joiningDate +
                '}';
    }
}
