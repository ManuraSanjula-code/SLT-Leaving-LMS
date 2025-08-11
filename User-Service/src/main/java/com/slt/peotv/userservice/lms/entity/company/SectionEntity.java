package com.slt.peotv.userservice.lms.entity.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="section")
@Setter
@Getter
@EqualsAndHashCode
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
}
