package com.slt.peotv.userservice.lms.shared.dto;

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
}
