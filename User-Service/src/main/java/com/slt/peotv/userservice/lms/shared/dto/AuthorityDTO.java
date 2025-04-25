package com.slt.peotv.userservice.lms.shared.dto;

import java.util.List;
import java.util.Objects;

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

	@Override
	public String toString() {
		return "AuthorityDTO [id=" + id + ", name=" + name + ", publicId=" + publicId + ", weight=" + weight
				+ ", roles=" + roles + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, publicId, roles, weight);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorityDTO other = (AuthorityDTO) obj;
		return id == other.id && Objects.equals(name, other.name) && Objects.equals(publicId, other.publicId)
				&& Objects.equals(roles, other.roles) && Objects.equals(weight, other.weight);
	}
}
