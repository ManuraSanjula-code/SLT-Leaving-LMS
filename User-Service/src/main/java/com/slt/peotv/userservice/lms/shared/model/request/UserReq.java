package com.slt.peotv.userservice.lms.shared.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import java.util.*;

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
    private Boolean roaster;
    private List<String> deleteAddresses;
    private Date joiningDate;

    @JsonIgnore
    @JsonProperty("additional")
    private Additional additional;

    @JsonProperty("admins")
    private List<String> admins = new ArrayList<>();

    @JsonProperty("addedAdmins")
    private List<String> addedAdmins = new ArrayList<>();

    @JsonProperty("deletedAdmins")
    private List<String> deletedAdmins = new ArrayList<>();

    public Additional getAdditional() {
        return additional;
    }

    public void setAdditional(Additional additional) {
        this.additional = additional;
    }

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

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public List<String> getAddedAdmins() {
        return addedAdmins;
    }

    public void setAddedAdmins(List<String> addedAdmins) {
        this.addedAdmins = addedAdmins;
    }

    public List<String> getDeletedAdmins() {
        return deletedAdmins;
    }

    public void setDeletedAdmins(List<String> deletedAdmins) {
        this.deletedAdmins = deletedAdmins;
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
                ", roaster=" + roaster +
                ", deleteAddresses=" + deleteAddresses +
                ", joiningDate=" + joiningDate +
                ", additional=" + additional +
                ", admins=" + admins +
                ", addedAdmins=" + addedAdmins +
                ", deletedAdmins=" + deletedAdmins +
                '}';
    }

    public static class Additional{
        @JsonIgnore
        private List<String> addedRoles;

        @JsonIgnore
        private List<String> addedSelections;

        @JsonIgnore
        private List<String> addedProfiles;

        @JsonIgnore
        private List<String> deleteRoles;

        @JsonIgnore
        private List<String> deleteSelections;

        @JsonIgnore
        private List<String> deleteProfiles;

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            Additional that = (Additional) object;
            return Objects.equals(addedRoles, that.addedRoles) && Objects.equals(addedSelections, that.addedSelections) && Objects.equals(addedProfiles, that.addedProfiles) && Objects.equals(deleteRoles, that.deleteRoles) && Objects.equals(deleteSelections, that.deleteSelections) && Objects.equals(deleteProfiles, that.deleteProfiles);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addedRoles, addedSelections, addedProfiles, deleteRoles, deleteSelections, deleteProfiles);
        }

        @Override
        public String toString() {
            return "Additional{" +
                    "addedRoles=" + addedRoles +
                    ", addedAuthorities=" + addedSelections +
                    ", addedProfiles=" + addedProfiles +
                    ", deleteRoles=" + deleteRoles +
                    ", deleteAuthorities=" + deleteSelections +
                    ", deleteProfiles=" + deleteProfiles +
                    '}';
        }

        public List<String> getAddedRoles() {
            return addedRoles;
        }

        public void setAddedRoles(List<String> addedRoles) {
            this.addedRoles = addedRoles;
        }

        public List<String> getAddedSelections() {
            return addedSelections;
        }

        public void setAddedSelections(List<String> addedSelections) {
            this.addedSelections = addedSelections;
        }

        public List<String> getAddedProfiles() {
            return addedProfiles;
        }

        public void setAddedProfiles(List<String> addedProfiles) {
            this.addedProfiles = addedProfiles;
        }

        public List<String> getDeleteRoles() {
            return deleteRoles;
        }

        public void setDeleteRoles(List<String> deleteRoles) {
            this.deleteRoles = deleteRoles;
        }

        public List<String> getDeleteSelections() {
            return deleteSelections;
        }

        public void setDeleteSelections(List<String> deleteSelections) {
            this.deleteSelections = deleteSelections;
        }

        public List<String> getDeleteProfiles() {
            return deleteProfiles;
        }

        public void setDeleteProfiles(List<String> deleteProfiles) {
            this.deleteProfiles = deleteProfiles;
        }
    }

}
