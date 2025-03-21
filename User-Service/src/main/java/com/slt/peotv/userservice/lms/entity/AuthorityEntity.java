package com.slt.peotv.userservice.lms.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


@Entity
@Table(name="authorities")
public class AuthorityEntity implements Serializable {

	private static final long serialVersionUID = -5828101164006114538L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable=false, length=20)
	private String name;

	@ManyToMany(mappedBy="authorities")
	@JsonIgnore
	@Column(nullable = false)
	private Collection<RoleEntity> roles;

	public AuthorityEntity() {}

	public AuthorityEntity(String name) {
		 this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Collection<RoleEntity> roles) {
		this.roles = roles;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		AuthorityEntity authority = (AuthorityEntity) o;
		return id == authority.id && Objects.equals(name, authority.name) && Objects.equals(roles, authority.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, roles);
	}

	@Override
	public String toString() {
		return "AuthorityEntity{" +
				"id=" + id +
				", name='" + name + '\'' +
				", roles=" + roles +
				'}';
	}
}
