package com.slt.peotv.lmsmangmentservice.entity.Absentee;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Builder
@Entity
@Table(name = "absentee")
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AbsenteeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String publicId;

    private Date date;

    private String employeeID;

    @Column(name = "Is_halfday")
    @Builder.Default
    private Boolean isHalfDay = false;

    @Column(name = "is_supervised_approved", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Boolean isSupervisedApproved = false;

    @Column(name = "is_HOD_approved", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Boolean isHODApproved = false;

    @Column(name = "audited", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Integer audited = 0;

    @Column(name = "is_nopay", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Integer isNoPay = 0;

    @Builder.Default
    private Boolean isPending = false;

    @Builder.Default
    private Boolean isAccepted = false;

    @Builder.Default
    private Boolean isLate = false;

    @Builder.Default
    private Boolean isAbsent = false;

    @Builder.Default
    private Boolean isUnSuccessfulAttdate = false;

    @Builder.Default
    private Boolean isLateCover = false;

    @Builder.Default
    private Date happenDate = null; // Explicitly initialized

    @Builder.Default
    private Boolean isArchived = false;

    @Builder.Default
    private String comment = ""; // Explicitly initialized
}
