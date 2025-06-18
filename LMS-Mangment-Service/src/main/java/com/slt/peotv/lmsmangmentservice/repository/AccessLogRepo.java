package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepo extends JpaRepository<AccessLogEntity, Long> {
    List<AccessLogEntity> findByLogDate(String logDate);

    @Query(value = "SELECT * FROM access_log WHERE log_date = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%d/%m/%Y')",
            nativeQuery = true)
    List<AccessLogEntity> findByYesterdayLogs();
}
