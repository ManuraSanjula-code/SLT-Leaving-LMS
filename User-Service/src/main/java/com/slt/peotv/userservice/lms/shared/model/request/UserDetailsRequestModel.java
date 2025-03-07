package com.slt.peotv.userservice.lms.shared.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
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

//	private List<AddressesRest> addresses;
	

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

	@Override
	public int hashCode() {
		return Objects.hash(active, addresses, email, firstName, gender, isSltEmp, isSltIntern, lastName, name,
				password, phone, profilePic, profiles, roles, section, sections, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDetailsRequestModel other = (UserDetailsRequestModel) obj;
		return Objects.equals(active, other.active) && Objects.equals(addresses, other.addresses)
				&& Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(gender, other.gender) && Objects.equals(isSltEmp, other.isSltEmp)
				&& Objects.equals(isSltIntern, other.isSltIntern) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(name, other.name) && Objects.equals(password, other.password)
				&& Objects.equals(phone, other.phone) && Objects.equals(profilePic, other.profilePic)
				&& Objects.equals(profiles, other.profiles) && Objects.equals(roles, other.roles)
				&& Objects.equals(section, other.section) && Objects.equals(sections, other.sections)
				&& Objects.equals(userId, other.userId);
	}
}
