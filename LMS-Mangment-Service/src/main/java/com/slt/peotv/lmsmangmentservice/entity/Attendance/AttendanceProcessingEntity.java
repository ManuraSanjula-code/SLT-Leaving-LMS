package com.slt.peotv.lmsmangmentservice.entity.Attendance;

import com.slt.peotv.lmsmangmentservice.entity.Enum.ProcessingFlag;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "attendance_processing")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceProcessingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_flag", nullable = false)
    private ProcessingFlag processingFlag;

    @Builder.Default
    @Column(name = "created_date")
    private Date createdDate = new Date();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
