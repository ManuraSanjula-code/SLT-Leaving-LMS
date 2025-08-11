package com.slt.peotv.userservice.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.shared.dto.UserAdminDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@EqualsAndHashCode(exclude = {"administratives", "adminUser"})
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
    private Collection<SectionEntity> sections =  new ArrayList<>();;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_profiles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "profile_id"))
    @Column(nullable = false)
    private Collection<ProfilesEntity> profiles =  new ArrayList<>();;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Column(nullable = false)
    private Collection<RoleEntity> roles =  new ArrayList<>();;

    @Column(nullable = false)
    private Date join_date;

    @Column(nullable = false)
    private Boolean roaster;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
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

    public void addAdmin(UserEntity admin) {
        if (admin != null) {
            this.myAdmins.add(admin);
            admin.getMySubordinates().add(this);
        }
    }

    public void removeAdmin(UserEntity admin) {
        if (admin != null) {
            this.myAdmins.remove(admin);
            admin.getMySubordinates().remove(this);
        }
    }

    public void addSubordinate(UserEntity subordinate) {
        if (subordinate != null) {
            this.mySubordinates.add(subordinate);
            subordinate.getMyAdmins().add(this);
        }
    }

    public void removeSubordinate(UserEntity subordinate) {
        if (subordinate != null) {
            this.mySubordinates.remove(subordinate);
            subordinate.getMyAdmins().remove(this);
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
}
