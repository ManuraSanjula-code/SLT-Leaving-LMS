package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepo extends CrudRepository<EmployeeEntity, Long> {
    Optional<EmployeeEntity> findByPublicId(String employeeID);
    Optional<EmployeeEntity> findBySltId(String sltId);
    Optional<EmployeeEntity> findByEmployeeId(String employeeId);
    Optional<EmployeeEntity> findByEmail(String email);
}
