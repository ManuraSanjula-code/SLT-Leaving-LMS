package com.slt.peotv.lmsmangmentservice.entity.Leave.category;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "user_leave_category_total")
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserLeaveCategoryTotalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;

    private String employeeID;

    @ManyToOne
    @JoinColumn(name = "leave_category_id")
    private LeaveCategoryEntity leaveCategory;
}

