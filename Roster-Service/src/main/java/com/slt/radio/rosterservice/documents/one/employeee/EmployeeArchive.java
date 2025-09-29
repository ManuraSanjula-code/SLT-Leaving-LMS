package com.slt.radio.rosterservice.documents.one.employeee;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Date;

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

    public EmployeeArchive() {}

    public EmployeeArchive(String id, String userId, String sltId, String employeeId,
                           String firstName, String lastName, String email, String profilePic,
                           Collection<String> roles, Collection<String> sections,
                           Collection<String> profiles, Integer isSltEmp, Integer isSltIntern,
                           Integer active, String phone, String gender,
                           Integer highestRolePriority, Date joiningDate, Boolean roaster) {
        this.id = id;
        this.userId = userId;
        this.sltId = sltId;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profilePic = profilePic;
        this.roles = roles;
        this.sections = sections;
        this.profiles = profiles;
        this.isSltEmp = isSltEmp;
        this.isSltIntern = isSltIntern;
        this.active = active != null ? active : 1;
        this.phone = phone;
        this.gender = gender;
        this.highestRolePriority = highestRolePriority;
        this.joiningDate = joiningDate;
        this.roaster = roaster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSltId() {
        return sltId;
    }

    public void setSltId(String sltId) {
        this.sltId = sltId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
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

    public Integer getHighestRolePriority() {
        return highestRolePriority;
    }

    public void setHighestRolePriority(Integer highestRolePriority) {
        this.highestRolePriority = highestRolePriority;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Boolean getRoaster() {
        return roaster;
    }

    public void setRoaster(Boolean roaster) {
        this.roaster = roaster;
    }

    public boolean isNew() {
        return id == null;
    }
}