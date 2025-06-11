package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceProcessingRepo extends JpaRepository<AttendanceProcessingEntity, Long> {
}
