package com.slt.peotv.lmsmangmentservice.entity.Attendance.types;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_type")
@Setter
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String publicId;
    private String shortName;
    private String description; // Fixed naming

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;
}