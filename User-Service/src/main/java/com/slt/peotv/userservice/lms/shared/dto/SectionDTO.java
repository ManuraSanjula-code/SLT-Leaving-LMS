package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;
import java.util.Objects;

public class SectionDTO {
    private long id;
    private String section;
    public String publicId;
    private List<UserDto> users;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public List<UserDto> getUsers() {
		return users;
	}
	public void setUsers(List<UserDto> users) {
		this.users = users;
	}
	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, publicId, section, users);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SectionDTO other = (SectionDTO) obj;
		return id == other.id && Objects.equals(publicId, other.publicId) && Objects.equals(section, other.section)
				&& Objects.equals(users, other.users);
	}
	@Override
	public String toString() {
		return "SectionDTO [id=" + id + ", section=" + section + ", publicId=" + publicId + ", users=" + users + "]";
	}
	
}
