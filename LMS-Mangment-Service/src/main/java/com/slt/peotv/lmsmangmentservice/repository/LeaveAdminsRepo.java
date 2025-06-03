package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveAdminsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveAdminsRepo extends JpaRepository<LeaveAdminsEntity, Integer> {
    Page<LeaveAdminsEntity> findByUserId(String userId, Pageable pageable);

    List<LeaveAdminsEntity> findBySltId(String sltId);

    List<LeaveAdminsEntity> findByEmployeeId(String employeeId);

    List<LeaveAdminsEntity> findByLeaveId(String leaveId);
}
