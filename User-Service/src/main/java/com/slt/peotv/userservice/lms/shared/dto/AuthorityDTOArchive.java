package com.slt.peotv.userservice.lms.shared.dto;

import java.util.Objects;

public class AuthorityDTOArchive {
	private long id;
	private String name;
	private String publicId;

	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
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
	@Override
	public String toString() {
		return "AuthorityDTOArchive [id=" + id + ", name=" + name + ", publicId=" + publicId + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, name, publicId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorityDTOArchive other = (AuthorityDTOArchive) obj;
		return id == other.id && Objects.equals(name, other.name) && Objects.equals(publicId, other.publicId);
	}
}
