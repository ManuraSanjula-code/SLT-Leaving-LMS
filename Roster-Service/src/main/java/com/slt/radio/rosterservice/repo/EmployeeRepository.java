package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.documents.one.employeee.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    Optional<Employee> findByName(String employeeId);
    List<Employee> findByTeamId(String teamId);

}

