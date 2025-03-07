package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceTypeRepo extends CrudRepository<AttendanceTypeEntity, Long> {
    Optional<AttendanceTypeEntity> findByShortName(String shortName);
}
