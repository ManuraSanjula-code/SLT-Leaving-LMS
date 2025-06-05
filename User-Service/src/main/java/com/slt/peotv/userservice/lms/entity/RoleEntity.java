package com.slt.peotv.userservice.lms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "roles")
@EqualsAndHashCode(callSuper = false)
@Data
public class RoleEntity implements Serializable {

    private static final long serialVersionUID = 5605260522147928803L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length = 20)
    private String name;

    private String publicId;
    private Integer ot;
    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    @JsonIgnore
    private Collection<UserEntity> users;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "roles_authorities",
            joinColumns = @JoinColumn(name = "roles_id"),
            inverseJoinColumns = @JoinColumn(name = "authorities_id", nullable = false))
    private Collection<AuthorityEntity> authorities =  new ArrayList<>();;

    @Column(name = "priority")
    private int priority;
   
}
