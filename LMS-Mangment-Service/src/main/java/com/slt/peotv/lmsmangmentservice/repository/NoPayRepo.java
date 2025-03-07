package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoPayRepo extends CrudRepository<NoPayEntity, Long> {
    List<NoPayEntity> findByEmployeeID(String employeeID);
    Optional<NoPayEntity> findByPublicId(String publicId);
}
