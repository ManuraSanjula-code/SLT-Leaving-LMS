package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenteeRepo extends JpaRepository<AbsenteeEntity, Long> {
    List<AbsenteeEntity> findByEmployeeID(String employeeID);
    Optional<AbsenteeEntity> findByPublicId(String publicId);
    Optional<AbsenteeEntity> findByEmployeeIDAndDate(String employeeID, Date date);
}
