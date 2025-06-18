package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.One.LMS.AccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends MongoRepository<AccessLog, String> {
    List<AccessLog> findByEmployeeIdAndLogDate(String employeeID, String logDate);
    List<AccessLog> findByLogDate(String logDate);
}
