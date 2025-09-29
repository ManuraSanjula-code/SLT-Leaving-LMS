package com.slt.peotv.userservice.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.shared.dto.UserAdminDto;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 5313493413859894403L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private String userId;

    @Column(nullable = false, unique = true)
    private String employeeId;

    @Column(nullable = false, unique = true)
    private String sltId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String encryptedPassword;

    @JsonIgnore
    private String emailVerificationToken;

    @JsonIgnore
    private Boolean emailVerificationStatus = false;

    @OneToMany(mappedBy = "userDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AddressEntity> addresses;

    private String profilePic;

    @Column(name = "gender", length = 1, nullable = false)
    private String gender;

    @Column(name = "phone", length = 45, nullable = false)
    private String phone;

    @Column(name = "is_slt_emp", columnDefinition = "int(10) unsigned default 0", nullable = false)
    private Integer isSltEmp;

    @Column(name = "is_slt_intern", columnDefinition = "int(10) unsigned default 0", nullable = false)
    private Integer isSltIntern;

    @Column(nullable = false)
    private Integer active = 1;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Column(nullable = false)
    @JoinTable(name = "user_sections", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "section_id"))
    private Collection<SectionEntity> sections = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_profiles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "profile_id"))
    @Column(nullable = false)
    private Collection<ProfilesEntity> profiles = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Column(nullable = false)
    private Collection<RoleEntity> roles = new ArrayList<>();

    @Column(nullable = false)
    private Date joinDate;

    @Column(nullable = false)
    private Boolean roaster;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "user_admins",
            joinColumns = @JoinColumn(name = "subordinate_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id"))
    @JsonIgnore
    private List<UserEntity> myAdmins = new ArrayList<>();

    @ManyToMany(mappedBy = "myAdmins", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<UserEntity> mySubordinates = new ArrayList<>();

    @Transient
    private List<UserAdminDto> administrativesDto;

    // Default constructor
    public UserEntity() {
    }

    // Getters and Setters
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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
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

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Collection<SectionEntity> getSections() {
        return sections;
    }

    public void setSections(Collection<SectionEntity> sections) {
        this.sections = sections;
    }

    public Collection<ProfilesEntity> getProfiles() {
        return profiles;
    }

    public void setProfiles(Collection<ProfilesEntity> profiles) {
        this.profiles = profiles;
    }

    public Collection<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Collection<RoleEntity> roles) {
        this.roles = roles;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Boolean getRoaster() {
        return roaster;
    }

    public void setRoaster(Boolean roaster) {
        this.roaster = roaster;
    }

    public List<UserEntity> getMyAdmins() {
        return myAdmins;
    }

    public void setMyAdmins(List<UserEntity> myAdmins) {
        this.myAdmins = myAdmins;
    }

    public List<UserEntity> getMySubordinates() {
        return mySubordinates;
    }

    public void setMySubordinates(List<UserEntity> mySubordinates) {
        this.mySubordinates = mySubordinates;
    }

    public List<UserAdminDto> getAdministrativesDto() {
        return administrativesDto;
    }

    public void setAdministrativesDto(List<UserAdminDto> administrativesDto) {
        this.administrativesDto = administrativesDto;
    }

    public void addAdmin(UserEntity admin) {
        if (admin != null) {
            // Initialize collections if null
            if (this.myAdmins == null) {
                this.myAdmins = new ArrayList<>();
            }
            if (admin.mySubordinates == null) {
                admin.mySubordinates = new ArrayList<>();
            }

            // Add relationships if they don't already exist
            if (!this.myAdmins.contains(admin)) {
                this.myAdmins.add(admin);
            }
            if (!admin.mySubordinates.contains(this)) {
                admin.mySubordinates.add(this);
            }
        }
    }

    public void removeAdmin(UserEntity admin) {
        if (admin != null) {
            if (this.myAdmins != null) {
                this.myAdmins.remove(admin);
            }
            if (admin.mySubordinates != null) {
                admin.mySubordinates.remove(this);
            }
        }
    }

    public void addSubordinate(UserEntity subordinate) {
        if (subordinate != null) {
            if (this.mySubordinates == null) {
                this.mySubordinates = new ArrayList<>();
            }
            if (subordinate.myAdmins == null) {
                subordinate.myAdmins = new ArrayList<>();
            }

            if (!this.mySubordinates.contains(subordinate)) {
                this.mySubordinates.add(subordinate);
            }
            if (!subordinate.myAdmins.contains(this)) {
                subordinate.myAdmins.add(this);
            }
        }
    }

    public void removeSubordinate(UserEntity subordinate) {
        if (subordinate != null) {
            if (this.mySubordinates != null) {
                this.mySubordinates.remove(subordinate);
            }
            if (subordinate.myAdmins != null) {
                subordinate.myAdmins.remove(this);
            }
        }
    }

    public void addSection(SectionEntity section) {
        if (section != null) {
            if (this.sections == null) {
                this.sections = new ArrayList<>();
            }
            if (!this.sections.contains(section)) {
                this.sections.add(section);
            }
        }
    }

    public void removeSection(SectionEntity section) {
        if (section != null && this.sections != null) {
            this.sections.remove(section);
        }
    }

    public void addProfile(ProfilesEntity profile) {
        if (profile != null) {
            if (this.profiles == null) {
                this.profiles = new ArrayList<>();
            }
            if (!this.profiles.contains(profile)) {
                this.profiles.add(profile);
            }
        }
    }

    public void removeProfile(ProfilesEntity profile) {
        if (profile != null && this.profiles != null) {
            this.profiles.remove(profile);
        }
    }

    public void addRole(RoleEntity role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new ArrayList<>();
            }
            if (!this.roles.contains(role)) {
                this.roles.add(role);
            }
        }
    }

    public void removeRole(RoleEntity role) {
        if (role != null && this.roles != null) {
            this.roles.remove(role);
        }
    }

    public boolean isActive() {
        return this.active != null && this.active == 1;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.toString();
    }

    public boolean isSltEmployee() {
        return this.isSltEmp != null && this.isSltEmp == 1;
    }

    public boolean isSltIntern() {
        return this.isSltIntern != null && this.isSltIntern == 1;
    }

    public boolean isEmailVerified() {
        return this.emailVerificationStatus != null && this.emailVerificationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id == that.id &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(sltId, that.sltId) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(encryptedPassword, that.encryptedPassword) &&
                Objects.equals(emailVerificationToken, that.emailVerificationToken) &&
                Objects.equals(emailVerificationStatus, that.emailVerificationStatus) &&
                Objects.equals(profilePic, that.profilePic) &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(isSltEmp, that.isSltEmp) &&
                Objects.equals(isSltIntern, that.isSltIntern) &&
                Objects.equals(active, that.active) &&
                Objects.equals(joinDate, that.joinDate) &&
                Objects.equals(roaster, that.roaster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, employeeId, sltId, firstName, lastName, email,
                encryptedPassword, emailVerificationToken, emailVerificationStatus,
                profilePic, gender, phone, isSltEmp, isSltIntern, active, joinDate, roaster);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", sltId='" + sltId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", isSltEmp=" + isSltEmp +
                ", isSltIntern=" + isSltIntern +
                ", active=" + active +
                ", joinDate=" + joinDate +
                ", roaster=" + roaster +
                ", emailVerificationStatus=" + emailVerificationStatus +
                '}';
    }
}