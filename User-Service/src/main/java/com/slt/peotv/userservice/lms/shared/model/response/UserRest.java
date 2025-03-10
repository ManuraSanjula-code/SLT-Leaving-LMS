package com.slt.peotv.userservice.lms.shared.model.response;

import java.util.Collection;
import java.util.List;

public class UserRest {
	private String userId;
	private String firstName;
	private String lastName;
	private String email;
	private List<AddressesRest> addresses;
	private String profilePic;
	private Collection<String> roles;
	private Collection<String> sections;
	private Collection<String> profiles;
	private Integer isSltEmp;
	private Integer isSltIntern;
	private Integer active = 1;
	private String phone;
	private String gender;

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

	public Collection<String> getRoles() {
		return roles;
	}

	public void setRoles(Collection<String> roles) {
		this.roles = roles;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getDefaultAddress() {
		return defaultAddress;
	}

	public void setDefaultAddress(String defaultAddress) {
		this.defaultAddress = defaultAddress;
	}

	private String defaultAddress;

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

	public List<AddressesRest> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<AddressesRest> addresses) {
		this.addresses = addresses;
	}

}
