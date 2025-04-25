package com.slt.peotv.userservice.lms.shared.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RoleDTO {
	private long id;
	private String name;
	private List<UserDtoArchive> users;
	private List<AuthorityDTO> authorities;
	private int priority;
	private String publicId;

	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}
	public RoleDTO(String name, int priority, Collection<AuthorityDTO> collect) {
		this.name = name;
		this.priority = priority;
		this.authorities = authorities != null ?
				new ArrayList<>(authorities) :
				new ArrayList<>();
	}

	public RoleDTO() {
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<UserDtoArchive> getUsers() {
		return users;
	}
	public void setUsers(List<UserDtoArchive> users) {
		this.users = users;
	}
	public List<AuthorityDTO> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<AuthorityDTO> authorities) {
		this.authorities = authorities;
	}
	@Override
	public String toString() {
		return "RoleDTO [id=" + id + ", name=" + name + ", users=" + users + ", authorities=" + authorities
				+ ", priority=" + priority + ", publicId=" + publicId + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(authorities, id, name, priority, publicId, users);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleDTO other = (RoleDTO) obj;
		return Objects.equals(authorities, other.authorities) && id == other.id && Objects.equals(name, other.name)
				&& priority == other.priority && Objects.equals(publicId, other.publicId)
				&& Objects.equals(users, other.users);
	}
}
