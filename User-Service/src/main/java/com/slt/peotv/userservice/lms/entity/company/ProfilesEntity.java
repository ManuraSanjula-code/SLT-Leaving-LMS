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
@Table(name="profile")
@Setter
@Getter
@EqualsAndHashCode
public class ProfilesEntity implements Serializable {
    private static final long serialVersionUID = 4466760523447920000L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "Name", length = 45, nullable = false)
    private String name;

    @Column(nullable = false)
    public String publicId;

    @Column(name = "work_start", length = 45, nullable = false)
    private String workStart;

    @Column(name = "work_ends", length = 45, nullable = false)
    private String workEnds;

    @Column(name = "ignore_sl", length = 45, nullable = false)
    private String ignoreSl;

    @Column(name = "grace_periode_start", length = 45, nullable = false)
    private String gracePeriodStart;

    @Column(name = "hd_start", length = 45, nullable = false)
    private String hdStart;

    @Column(name = "sl_start_morning", length = 45, nullable = false)
    private String slStartMorning;

    @Column(name = "sl_start_evening", length = 45, nullable = false)
    private String slStartEvening;

    @Column(name = "possible_fp_locations", columnDefinition = "TEXT", nullable = false)
    private String possibleFpLocations;

    @Column(name = "default_hrs", length = 45, nullable = false)
    private String defaultHrs;

    @Column(name = "hd_hrs", length = 45, nullable = false)
    private String hdHrs;

    @Column(name = "min_hrs_for_sl", length = 45, nullable = false)
    private String minHrsForSl;

    @Column(name = "short_leave_count", length = 45, nullable = false)
    private String shortLeaveCount;

    @Column(name = "hd_ends_morning", length = 45, nullable = false)
    private String hdEndsMorning;

    @Column(name = "flexi_days", length = 45, nullable = false)
    private String flexiDays;

    @Column(name = "flexi_hrs_start", length = 45, nullable = false)
    private String flexiHrsStart;

    @ManyToMany(mappedBy = "profiles")
	@JsonIgnore
    private Collection<UserEntity> users =  new ArrayList<>();;
}
