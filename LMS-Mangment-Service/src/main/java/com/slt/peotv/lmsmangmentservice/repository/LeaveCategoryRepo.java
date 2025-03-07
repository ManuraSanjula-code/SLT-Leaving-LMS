package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveCategoryRepo extends CrudRepository<LeaveCategoryEntity, Long> {
    Optional<LeaveCategoryEntity> findByName(String name);
    Optional<LeaveCategoryEntity> findByPublicId(String publicId);
}
