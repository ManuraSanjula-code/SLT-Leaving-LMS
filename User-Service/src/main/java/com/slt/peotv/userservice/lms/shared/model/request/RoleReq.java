package com.slt.peotv.userservice.lms.shared.model.request;

import java.util.List;

public class RoleReq {
    private String name;
    private String roleId;
    private List<String> addedUsers;
    private List<String> deletedUsers;
    private List<String> addedAuthorities;
    private List<String> deletedAuthorities;
    private Integer priority;
    private String publicId;

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAddedUsers() {
        return addedUsers;
    }

    public void setAddedUsers(List<String> addedUsers) {
        this.addedUsers = addedUsers;
    }

    public List<String> getDeletedUsers() {
        return deletedUsers;
    }

    public void setDeletedUsers(List<String> deletedUsers) {
        this.deletedUsers = deletedUsers;
    }

    public List<String> getAddedAuthorities() {
        return addedAuthorities;
    }

    public void setAddedAuthorities(List<String> addedAuthorities) {
        this.addedAuthorities = addedAuthorities;
    }

    public List<String> getDeletedAuthorities() {
        return deletedAuthorities;
    }

    public void setDeletedAuthorities(List<String> deletedAuthorities) {
        this.deletedAuthorities = deletedAuthorities;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
