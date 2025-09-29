package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.documents.one.lms.AccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends MongoRepository<AccessLog, String> {
    List<AccessLog> findByEmployeeIdAndLogDate(String employeeID, String logDate);
    List<AccessLog> findByLogDate(String logDate);
    boolean existsByEmployeeIdAndLogDateAndLogTimeAndTerminalIdAndInOut(String employeeId, String logDate, String logTime, String terminalId, String inOut);
}
