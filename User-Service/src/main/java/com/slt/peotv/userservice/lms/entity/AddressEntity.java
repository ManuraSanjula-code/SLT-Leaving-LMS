package com.slt.peotv.userservice.lms.entity;

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity(name="addresses")
public class AddressEntity implements Serializable {

	private static final long serialVersionUID = 7809200551672852690L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length=30, nullable=false)
	private String addressId;

	@Column(length=15, nullable=false)
	private String city;

	@Column(length=15, nullable=false)
	private String country;

	@Column(length=100, nullable=false)
	private String streetName;

	@Column(length=7, nullable=false)
	private String postalCode;

	@Column(nullable=false)
	private Boolean isDefault = Boolean.FALSE;

	public AddressEntity() {
		this.isDefault = false;
	}
	@ManyToOne
	@JoinColumn(name="users_id", nullable=false)
	@JsonBackReference
	private UserEntity userDetails;

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

	public String getAddressId() {
		return addressId;
	}

	public void setAddressId(String addressId) {
		this.addressId = addressId;
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

	public UserEntity getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserEntity userDetails) {
		this.userDetails = userDetails;
	}


	@Override
	public String toString() {
		return "AddressEntity{" +
				"id=" + id +
				", addressId='" + addressId + '\'' +
				", city='" + city + '\'' +
				", country='" + country + '\'' +
				", streetName='" + streetName + '\'' +
				", postalCode='" + postalCode + '\'' +
				", userDetails=" + userDetails +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		AddressEntity that = (AddressEntity) o;
		return id == that.id && Objects.equals(addressId, that.addressId) && Objects.equals(city, that.city) && Objects.equals(country, that.country) && Objects.equals(streetName, that.streetName) && Objects.equals(postalCode, that.postalCode) && Objects.equals(isDefault, that.isDefault) && Objects.equals(userDetails, that.userDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, addressId, city, country, streetName, postalCode, isDefault, userDetails);
	}
}
