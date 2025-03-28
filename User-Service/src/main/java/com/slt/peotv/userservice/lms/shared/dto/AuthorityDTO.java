package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;

public class AuthorityDTO {
	public AuthorityDTO() {
	}

	public AuthorityDTO(String name, long id) {
		this.id = id;
		this.name = name;
	}

	private long id;
	private String name;
	private String publicId;
	private Integer weight; // Higher = more privileged
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
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
	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}
}
