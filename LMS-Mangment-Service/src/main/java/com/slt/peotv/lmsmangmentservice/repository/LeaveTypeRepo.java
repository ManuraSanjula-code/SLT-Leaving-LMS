package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LeaveTypeRepo extends CrudRepository<LeaveTypeEntity, Long> {
    Optional<LeaveTypeEntity> findByName(String name);
    Optional<LeaveTypeEntity> findByPublicId(String publicId);
}
