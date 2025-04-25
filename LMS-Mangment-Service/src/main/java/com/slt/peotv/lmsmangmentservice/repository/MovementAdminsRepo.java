package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementAdminsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementAdminsRepo extends JpaRepository<MovementAdminsEntity, Integer> {
    Page<MovementAdminsEntity> findByUserId(String userId, Pageable pageable);

    List<MovementAdminsEntity> findBySltId(String sltId);

    List<MovementAdminsEntity> findByEmployeeId(String employeeId);
}
