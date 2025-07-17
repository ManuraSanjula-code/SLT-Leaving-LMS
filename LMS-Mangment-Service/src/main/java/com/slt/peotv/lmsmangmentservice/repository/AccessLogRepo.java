package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

@Repository
public interface AccessLogRepo extends JpaRepository<AccessLogEntity, Long> {
    List<AccessLogEntity> findByLogDate(String logDate);

    @Query(value = "SELECT * FROM access_log WHERE log_date = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%d/%m/%Y')",
            nativeQuery = true)
    List<AccessLogEntity> findByYesterdayLogs();

    @Query(value = "SELECT * FROM access_log WHERE log_date = DATE_FORMAT(CURDATE(), '%d/%m/%Y')",
            nativeQuery = true)
    List<AccessLogEntity> findByTodayLogs();

    boolean existsByEmployeeIdAndLogDateAndLogTimeAndTerminalId(String employeeId, String logDate, String logTime, String terminalId);

    @Modifying
    @Query(value = "INSERT INTO access_log (employee_id, log_date, log_time, terminal_id, in_out, read_status, processed, etl_run_time, is_manual, created_date, updated_date, is_active) " +
            "VALUES (:employeeId, :logDate, :logTime, :terminalId, :inOut, :readStatus, :processed, :etlRunTime, :isManual, :createdDate, :updatedDate, :isActive) " +
            "ON DUPLICATE KEY UPDATE updated_date = VALUES(updated_date)",
            nativeQuery = true)
    void insertOrUpdateOnDuplicate(@Param("employeeId") String employeeId,
                                   @Param("logDate") String logDate,
                                   @Param("logTime") String logTime,
                                   @Param("terminalId") String terminalId,
                                   @Param("inOut") String inOut,
                                   @Param("readStatus") String readStatus,
                                   @Param("processed") Integer processed,
                                   @Param("etlRunTime") Date etlRunTime,
                                   @Param("isManual") Boolean isManual,
                                   @Param("createdDate") Date createdDate,
                                   @Param("updatedDate") Date updatedDate,
                                   @Param("isActive") Boolean isActive);

}
