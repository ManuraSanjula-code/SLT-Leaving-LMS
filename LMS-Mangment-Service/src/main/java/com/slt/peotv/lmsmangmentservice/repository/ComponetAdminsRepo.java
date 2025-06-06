package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponetAdminsRepo extends JpaRepository<ComponetAdminsEntity, Integer> {
    Page<ComponetAdminsEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    List<ComponetAdminsEntity> findByEmployee(EmployeeEntity employee);
    List<ComponetAdminsEntity> findByComponetID(String componetID);
}

