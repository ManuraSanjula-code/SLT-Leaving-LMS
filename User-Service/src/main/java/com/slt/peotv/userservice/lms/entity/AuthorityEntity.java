package com.slt.peotv.userservice.lms.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;


@Entity
@Table(name="authorities")
public class AuthorityEntity implements Serializable {

	private static final long serialVersionUID = -5828101164006114538L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable=false, length=20, unique=true)
	private String name;

	private String publicId;

	@ManyToMany(mappedBy="authorities")
	@JsonIgnore
	@Column(nullable = false)
	private Collection<RoleEntity> roles =  new ArrayList<>();;
	
	public AuthorityEntity() {}

	public AuthorityEntity(String name) {
		 this.name = name;
	}
	private int weight;

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

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public Collection<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Collection<RoleEntity> roles) {
		this.roles = roles;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		AuthorityEntity that = (AuthorityEntity) o;
		return weight == that.weight && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(publicId, that.publicId) && Objects.equals(roles, that.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, publicId, roles, weight);
	}
}
