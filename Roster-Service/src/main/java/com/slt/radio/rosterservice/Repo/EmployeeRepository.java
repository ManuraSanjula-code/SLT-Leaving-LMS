package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.Employeee.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    Optional<Employee> findByName(String employeeId);
}

