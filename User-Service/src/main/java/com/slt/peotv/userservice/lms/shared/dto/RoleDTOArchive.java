package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;

public class RoleDTOArchive {
	private long id;
	private String name;
	private List<UserDtoArchive> users;
	private List<AuthorityDTOArchive> authorities;
	private int priority;
	private String publicId;

	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
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
	public List<AuthorityDTOArchive> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<AuthorityDTOArchive> authorities) {
		this.authorities = authorities;
	}
}
