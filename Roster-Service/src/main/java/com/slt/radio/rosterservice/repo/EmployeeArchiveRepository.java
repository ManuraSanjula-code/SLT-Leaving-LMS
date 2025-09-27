package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.model.one.employeee.EmployeeArchive;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeArchiveRepository extends MongoRepository<EmployeeArchive, String> {
    EmployeeArchive findByUserId(String userId);
    Optional<EmployeeArchive> findByEmployeeId(String employeeId);
    Optional<EmployeeArchive> findBySltId(String sltId);
    Optional<EmployeeArchive> findByEmail(String email);
    List<EmployeeArchive> findByRoaster(Boolean roaster);
}

