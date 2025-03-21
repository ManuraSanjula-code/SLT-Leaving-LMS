package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class AuthorityDTO {
	private long id;
	private String name;
	@JsonBackReference
	private List<RoleDTO> roles;
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
	public List<RoleDTO> getRoles() {
		return roles;
	}
	public void setRoles(List<RoleDTO> roles) {
		this.roles = roles;
	}
}
