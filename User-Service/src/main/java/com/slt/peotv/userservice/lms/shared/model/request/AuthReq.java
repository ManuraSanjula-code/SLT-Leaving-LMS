package com.slt.peotv.userservice.lms.shared.model.request;

public class AuthReq {
    private String newName;
    private String oldName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }
}
