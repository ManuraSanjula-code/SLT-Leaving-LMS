package com.slt.peotv.userservice.lms.entity.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import javax.persistence.*;

@Entity
@Table(name="section")
public class SectionEntity implements Serializable {
    private static final long serialVersionUID = 5566760523447927363L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    @Column(nullable=false, length=20, unique=true)
    private String section;

    @Column(nullable = false)
    public String publicId;

    @ManyToMany(mappedBy="sections")
	@JsonIgnore
    private Collection<UserEntity> users =  new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Collection<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Collection<UserEntity> users) {
        this.users = users;
    }

    public void addUser(UserEntity user) {
        if (user != null) {
            if (this.users == null) {
                this.users = new ArrayList<>();
            }
            if (!this.users.contains(user)) {
                this.users.add(user);
                user.addSection(this);
            }
        }
    }

    public void removeUser(UserEntity user) {
        if (user != null && this.users != null) {
            this.users.remove(user);
            user.removeSection(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SectionEntity that = (SectionEntity) o;
        return id == that.id && Objects.equals(section, that.section) && Objects.equals(publicId, that.publicId) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, section, publicId, users);
    }
}
