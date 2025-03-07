package com.slt.peotv.userservice.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="users")
public class UserEntity implements Serializable {
 
	private static final long serialVersionUID = 5313493413859894403L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private long id;
	
	private String userId;

	private String employeeId;
	
	private String sltId;

	private String firstName;
	
	private String lastName;
	
	private String email;
	
	@Column(nullable=false)
	@JsonIgnore
	private String encryptedPassword;
	
	@JsonIgnore
	private String emailVerificationToken;
	
	@JsonIgnore
	private Boolean emailVerificationStatus = false;

	@OneToMany(mappedBy="userDetails", cascade=CascadeType.ALL)
	private List<AddressEntity> addresses;
	
	private String profilePic;

	@Column(name = "gender", length = 1)
	private String gender;

	@Column(name = "phone", length = 45)
	private String phone;

	@Column(name = "is_slt_emp",columnDefinition = "int(10) unsigned default 0")
	private Integer isSltEmp;
	@Column(name = "is_slt_intern",columnDefinition = "int(10) unsigned default 0")
	private Integer isSltIntern;

	@Column(nullable=false)
	private Integer active = 1;

	@ManyToMany(cascade= { CascadeType.PERSIST }, fetch = FetchType.EAGER )
	@JoinTable(name="users_roles",
			joinColumns=@JoinColumn(name="users_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="roles_id",referencedColumnName="id"))
	@Column(nullable=false)
	@JsonManagedReference
	private Collection<RoleEntity> roles;

	@ManyToMany(cascade= { CascadeType.PERSIST }, fetch = FetchType.EAGER )
	@JoinTable(name="users_profile",
			joinColumns=@JoinColumn(name="users_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="profile_id",referencedColumnName="id"))
	@Column(nullable=false)
	private Collection<ProfilesEntity> profiles;

	@ManyToMany(cascade= { CascadeType.PERSIST }, fetch = FetchType.EAGER )
	@JoinTable(name="users_section",
			joinColumns=@JoinColumn(name="users_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="section_id",referencedColumnName="id"))
	@Column(nullable=false)
	private Collection<SectionEntity> sections;
	
	private Date join_date;
	
	@OneToOne
	private UserEntity hod;
	@OneToOne
	private UserEntity supervisor;
	
	private Boolean roaster;

	public String getSltId() {
		return sltId;
	}

	public void setSltId(String sltId) {
		this.sltId = sltId;
	}

	public UserEntity getHod() {
		return hod;
	}

	public void setHod(UserEntity hod) {
		this.hod = hod;
	}

	public UserEntity getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(UserEntity supervisor) {
		this.supervisor = supervisor;
	}

	public Boolean getRoaster() {
		return roaster;
	}

	public void setRoaster(Boolean roaster) {
		this.roaster = roaster;
	}

	public Date getJoin_date() {
		return join_date;
	}

	public void setJoin_date(Date join_date) {
		this.join_date = join_date;
	}

	public List<AddressEntity> getAddresses() {
		return addresses;
	}

	public Collection<RoleEntity> getRoles() {
		return roles;
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

	public void setRoles(Collection<RoleEntity> roles) {
		this.roles = roles;
	}

	public Collection<ProfilesEntity> getProfiles() {
		return profiles;
	}

	public void setProfiles(Collection<ProfilesEntity> profiles) {
		this.profiles = profiles;
	}

	public Collection<SectionEntity> getSections() {
		return sections;
	}

	public void setSections(Collection<SectionEntity> sections) {
		this.sections = sections;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, addresses, email, emailVerificationStatus, emailVerificationToken, employeeId,
				encryptedPassword, firstName, gender, id, isSltEmp, isSltIntern, join_date, lastName, phone, profilePic,
				profiles, roles, sections, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		return Objects.equals(active, other.active) && Objects.equals(addresses, other.addresses)
				&& Objects.equals(email, other.email)
				&& Objects.equals(emailVerificationStatus, other.emailVerificationStatus)
				&& Objects.equals(emailVerificationToken, other.emailVerificationToken)
				&& Objects.equals(employeeId, other.employeeId)
				&& Objects.equals(encryptedPassword, other.encryptedPassword)
				&& Objects.equals(firstName, other.firstName) && Objects.equals(gender, other.gender) && id == other.id
				&& Objects.equals(isSltEmp, other.isSltEmp) && Objects.equals(isSltIntern, other.isSltIntern)
				&& Objects.equals(join_date, other.join_date) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(phone, other.phone) && Objects.equals(profilePic, other.profilePic)
				&& Objects.equals(profiles, other.profiles) && Objects.equals(roles, other.roles)
				&& Objects.equals(sections, other.sections) && Objects.equals(userId, other.userId);
	}
}
