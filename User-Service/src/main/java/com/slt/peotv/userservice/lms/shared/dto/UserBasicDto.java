package com.slt.peotv.userservice.lms.shared.dto;

import java.util.Objects;

public class UserBasicDto {
    private String userId;
    private String firstName;
    private String lastName;

    public UserBasicDto() {
    }

    public UserBasicDto(String userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
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

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserBasicDto other = (UserBasicDto) obj;
		return Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(userId, other.userId);
	}

	@Override
	public String toString() {
		return "UserBasicDto [userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}
}
