package com.slt.peotv.lmsmangmentservice.entity.Leave;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "leave_requests")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveEntity {

    @Column(nullable = false)
    public String publicId;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String employeeID;

    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @Column(name = "from_date", nullable = false)
    private Date fromDate;

    @Column(name = "to_date", nullable = false)
    private Date toDate;

    /*@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "leave_category_id", referencedColumnName = "id", nullable = false)
    private LeaveCategoryEntity leaveCategory;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "leave_type_id", referencedColumnName = "id", nullable = false)
    private LeaveTypeEntity leaveType;*/

    @ManyToOne
    @JoinColumn(name = "leave_category_id", foreignKey = @ForeignKey(name = "FK_leave_category"))
    private LeaveCategoryEntity leaveCategory;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", foreignKey = @ForeignKey(name = "FK_leave_type"))
    private LeaveTypeEntity leaveType;


    @Column(name = "is_supervised_approved", nullable = false, columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Boolean isSupervisedApproved = false;

    @Column(name = "is_HOD_approved", nullable = false, columnDefinition = "int(10) unsigned default '0'")
    @Builder.Default
    private Boolean isHODApproved = false;

    @Column(name = "is_nopay", columnDefinition = "int(10) unsigned default '0'")
    private Integer isNoPay = 0;

    @Column(name = "num_of_days")
    private Long numOfDays;

    @Column(name = "description")
    private String description;

    @Column(name = "is_halfday")
    private Boolean isHalfDay;

    @Builder.Default
    private Boolean unSuccessful = false;
    @Builder.Default
    private Boolean isLate = false;
    @Builder.Default
    private Boolean isLateCover = false;
    @Builder.Default
    private Boolean isShort_Leave = false;
    @Builder.Default
    private Boolean isPending = false;
    @Builder.Default
    private Boolean isAccepted = false;
    @Builder.Default
    private Boolean notUsed = false;
    @Builder.Default
    private Boolean isCanceled = false;
    @Builder.Default
    private Boolean isManualRequest = false;
    private Date happenDate;

}
