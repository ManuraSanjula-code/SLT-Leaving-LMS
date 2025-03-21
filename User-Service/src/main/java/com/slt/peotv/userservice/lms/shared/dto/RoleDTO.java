package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;

public class RoleDTO {
	private long id;
	private String name;
	private List<UserDtoArchive> users;
	private List<AuthorityDTO> authorities;
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
}
