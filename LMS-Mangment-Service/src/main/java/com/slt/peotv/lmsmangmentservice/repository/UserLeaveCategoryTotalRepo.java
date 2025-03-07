package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveCategoryTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLeaveCategoryTotalRepo extends CrudRepository<UserLeaveCategoryTotalEntity, Long> {
    Optional<UserLeaveCategoryTotalEntity> findByPublicId(String publicId);
    List<UserLeaveCategoryTotalEntity> findByLeaveCategoryAndEmployeeID(LeaveCategoryEntity leaveCategory, String employeeID);
    List<UserLeaveCategoryTotalEntity> findByEmployeeID(String employeeID);
}
