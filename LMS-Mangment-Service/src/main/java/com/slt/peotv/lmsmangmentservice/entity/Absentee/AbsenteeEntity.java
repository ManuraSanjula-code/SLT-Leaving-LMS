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
@ToString
public class AbsenteeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String publicId;

    private Date date;

    private String employeeID;
    private String userId;

    @Column(name = "audited", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Integer audited = 0;

    @Column(name = "is_nopay", columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Integer isNoPay = 0;
}
