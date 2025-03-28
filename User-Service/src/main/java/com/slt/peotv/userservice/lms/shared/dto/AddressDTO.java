package com.slt.peotv.userservice.lms.shared.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Objects;

public class AddressDTO {
	private long id;
	private String addressId;
	private String city;
	private String country;
	private String streetName;
	private String postalCode;
	@JsonBackReference 
	private UserDto userDetails;
	private Boolean isDefault;

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Boolean isDefault() {
		return isDefault;
	}

	public void setDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public UserDto getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDto userDetails) {
		this.userDetails = userDetails;
	}

	public String getAddressId() {
		return addressId;
	}

	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}


	@Override
	public String toString() {
		return "AddressDTO{" +
				"id=" + id +
				", addressId='" + addressId + '\'' +
				", city='" + city + '\'' +
				", country='" + country + '\'' +
				", streetName='" + streetName + '\'' +
				", postalCode='" + postalCode + '\'' +
				", userDetails=" + userDetails +
				", isDefault=" + isDefault +
				'}';
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AddressDTO that = (AddressDTO) o;
		return Objects.equals(city, that.city) &&
				Objects.equals(streetName, that.streetName) &&
				Objects.equals(postalCode, that.postalCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, streetName, postalCode);
	}
}
