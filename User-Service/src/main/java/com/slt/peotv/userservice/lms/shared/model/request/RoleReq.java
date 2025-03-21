package com.slt.peotv.userservice.lms.shared.model.request;

import java.util.List;

public class RoleReq {
    private String name;
    private List<String> addedUsers;
    private List<String> deletedUsers;
    private List<String> addedAuthorities;
    private List<String> deletedAuthorities;

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
}
