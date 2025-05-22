package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepo extends JpaRepository<AccessLogEntity, Long> {
    List<AccessLogEntity> findByLogDate(String logDate);
}
