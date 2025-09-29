package com.slt.peotv.userservice.lms.shared.model.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserDetailsRequestModel {

	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private List<AddressRequestModel> addresses = new ArrayList<>();
	private String name;
	private String section;
	private String userId;
	private String profilePic;
	private Collection<String> roles;
	private Collection<String> sections;
	private Collection<String> profiles;
	private Integer isSltEmp;
	private Integer isSltIntern;
	private Integer active = 1;
	private String phone;
	private String gender;
	private Boolean isDefault;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<AddressRequestModel> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<AddressRequestModel> addresses) {
		this.addresses = addresses;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public Boolean getDefault() {
		return isDefault;
	}

	public void setDefault(Boolean aDefault) {
		isDefault = aDefault;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		UserDetailsRequestModel that = (UserDetailsRequestModel) o;
		return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(addresses, that.addresses) && Objects.equals(name, that.name) && Objects.equals(section, that.section) && Objects.equals(userId, that.userId) && Objects.equals(profilePic, that.profilePic) && Objects.equals(roles, that.roles) && Objects.equals(sections, that.sections) && Objects.equals(profiles, that.profiles) && Objects.equals(isSltEmp, that.isSltEmp) && Objects.equals(isSltIntern, that.isSltIntern) && Objects.equals(active, that.active) && Objects.equals(phone, that.phone) && Objects.equals(gender, that.gender) && Objects.equals(isDefault, that.isDefault);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, email, password, addresses, name, section, userId, profilePic, roles, sections, profiles, isSltEmp, isSltIntern, active, phone, gender, isDefault);
	}
}
