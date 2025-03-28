package com.slt.peotv.userservice.lms.shared.model.request;

import java.util.List;

public class SectionReq {
    private String publicId;
    private String section;
    private List<String> addedUsers;
    private List<String> deletedUsers;

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
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
